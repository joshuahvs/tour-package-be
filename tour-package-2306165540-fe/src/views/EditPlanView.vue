<template>
  <div class="edit-plan-container">
    <h1>Edit Plan</h1>

    <div v-if="loading" class="loading">Loading plan data...</div>
    <div v-else-if="error" class="error">{{ error }}</div>

    <form v-else @submit.prevent="handleSubmit" class="edit-plan-form">
      <div class="form-group">
        <label for="planName">Plan Name <span class="required">*</span></label>
        <input
          id="planName"
          v-model="formData.planName"
          type="text"
          required
          placeholder="Enter plan name"
        />
      </div>

      <div class="form-group">
        <label for="activityType">Activity Type</label>
        <input
          id="activityType"
          :value="planDetail?.activityType || ''"
          type="text"
          readonly
          class="readonly"
        />
      </div>

      <div class="form-group">
        <label for="startDate">Start Date <span class="required">*</span></label>
        <input
          id="startDate"
          v-model="formData.startDate"
          type="datetime-local"
          required
        />
      </div>

      <div class="form-group">
        <label for="endDate">End Date <span class="required">*</span></label>
        <input
          id="endDate"
          v-model="formData.endDate"
          type="datetime-local"
          required
        />
      </div>

      <div class="form-group">
        <label for="startLocation">Start Location <span class="required">*</span></label>
        <select id="startLocation" v-model="formData.startLocation" required>
          <option value="">Select start location</option>
          <option v-for="location in locations" :key="location.code" :value="location.name">
            {{ location.name }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label for="endLocation">End Location <span class="required">*</span></label>
        <select id="endLocation" v-model="formData.endLocation" required>
          <option value="">Select end location</option>
          <option v-for="location in locations" :key="location.code" :value="location.name">
            {{ location.name }}
          </option>
        </select>
      </div>

      <div class="form-actions">
        <button type="button" @click="handleCancel" class="btn-cancel">Cancel</button>
        <button type="submit" :disabled="submitting" class="btn-submit">
          {{ submitting ? 'Saving...' : 'Save Changes' }}
        </button>
      </div>

      <div v-if="submitError" class="error-message">{{ submitError }}</div>
      <div v-if="submitSuccess" class="success-message">{{ submitSuccess }}</div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { planApi } from '@/services/plan.service';
import type { PlanDetailData, LocationData, UpdatePlanRequest } from '@/interface/plan.interface';

const router = useRouter();
const route = useRoute();

const planId = route.params.id as string;

const loading = ref(true);
const error = ref('');
const planDetail = ref<PlanDetailData | null>(null);
const locations = ref<LocationData[]>([]);

const formData = ref<UpdatePlanRequest>({
  planName: '',
  startDate: '',
  endDate: '',
  startLocation: '',
  endLocation: ''
});

const submitting = ref(false);
const submitError = ref('');
const submitSuccess = ref('');

const formatDateForInput = (dateString: string): string => {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}`;
};

const formatDateForAPI = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toISOString();
};

const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0,
  }).format(amount);
};

const formatDateTime = (dateString: string): string => {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('id-ID', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};

const loadPlanData = async () => {
  try {
    loading.value = true;
    error.value = '';

    const [planData, locationsData] = await Promise.all([
      planApi.getPlanDetail(planId),
      planApi.getLocations()
    ]);

    planDetail.value = planData;
    
    // Pre-populate form with existing data
    formData.value = {
      planName: planData.planName,
      startDate: formatDateForInput(planData.startDate),
      endDate: formatDateForInput(planData.endDate),
      startLocation: planData.startLocation,
      endLocation: planData.endLocation
    };

    locations.value = locationsData;
  } catch (err: any) {
    error.value = err.message || 'Failed to load plan data';
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  try {
    submitting.value = true;
    submitError.value = '';
    submitSuccess.value = '';

    // Validate dates
    const startDate = new Date(formData.value.startDate);
    const endDate = new Date(formData.value.endDate);

    if (endDate <= startDate) {
      submitError.value = 'End date must be after start date';
      return;
    }

    // Prepare data for API
    const updateData: UpdatePlanRequest = {
      planName: formData.value.planName,
      startDate: formatDateForAPI(formData.value.startDate),
      endDate: formatDateForAPI(formData.value.endDate),
      startLocation: formData.value.startLocation,
      endLocation: formData.value.endLocation
    };

    const planData = await planApi.updatePlan(planId, updateData);

    submitSuccess.value = 'Plan updated successfully! Redirecting...';
    setTimeout(() => {
      router.push(`/plans/${planId}`);
    }, 1500);
  } catch (err: any) {
    submitError.value = err.message || 'Failed to update plan';
  } finally {
    submitting.value = false;
  }
};

const handleCancel = () => {
  router.push(`/plans/${planId}`);
};

onMounted(() => {
  loadPlanData();
});
</script>

<style scoped>
.edit-plan-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

h1 {
  margin-bottom: 2rem;
  color: #333;
}

.loading,
.error {
  text-align: center;
  padding: 2rem;
  font-size: 1.1rem;
}

.error {
  color: #dc3545;
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
}

.edit-plan-form {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #333;
}

.required {
  color: #dc3545;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.form-group input.readonly {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.btn-cancel,
.btn-submit {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-cancel {
  background-color: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background-color: #5a6268;
}

.btn-submit {
  background-color: #007bff;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-submit:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.error-message {
  margin-top: 1rem;
  padding: 1rem;
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
}

.success-message {
  margin-top: 1rem;
  padding: 1rem;
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
  border-radius: 4px;
}
</style>
