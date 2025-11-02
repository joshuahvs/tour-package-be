<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
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

    // Load locations first to ensure they're available for the select dropdowns
    locations.value = await planApi.getLocations();
    console.log('Loaded locations:', locations.value);
    
    // Then load plan data
    const planData = await planApi.getPlanDetail(planId);
    planDetail.value = planData;
    console.log('Loaded plan data:', planData);
    console.log('Plan startLocation:', planData.startLocation);
    console.log('Plan endLocation:', planData.endLocation);
    
    // Wait for next tick to ensure DOM is updated with locations
    await nextTick();
    
    // Pre-populate form with existing data - ensure exact match with location names
    formData.value.planName = planData.planName;
    formData.value.startDate = formatDateForInput(planData.startDate);
    formData.value.endDate = formatDateForInput(planData.endDate);
    formData.value.startLocation = planData.startLocation;
    formData.value.endLocation = planData.endLocation;
    
    console.log('Form data after setting:', formData.value);
    
    // Additional check: verify if the location values match any option
    const startLocationMatch = locations.value.find(loc => loc.name === planData.startLocation);
    const endLocationMatch = locations.value.find(loc => loc.name === planData.endLocation);
    console.log('Start location match:', startLocationMatch);
    console.log('End location match:', endLocationMatch);
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

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold text-gray-800 mb-8">Edit Plan</h1>

    <div v-if="loading" class="text-center text-gray-500 py-10 text-lg">Loading plan data...</div>
    <div v-else-if="error" class="text-red-700 bg-red-100 border border-red-300 rounded-md px-4 py-3 mb-4 text-center">
      {{ error }}
    </div>

    <form v-else @submit.prevent="handleSubmit" class="bg-white rounded-xl shadow-md p-6 space-y-6">
      <!-- Plan Name -->
      <div>
        <label for="planName" class="block text-sm font-semibold text-gray-700 mb-2">
          Plan Name <span class="text-red-500">*</span>
        </label>
        <input
          id="planName"
          v-model="formData.planName"
          type="text"
          required
          placeholder="Enter plan name"
          class="w-full border border-gray-300 rounded-md p-2.5 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <!-- Activity Type -->
      <div>
        <label for="activityType" class="block text-sm font-semibold text-gray-700 mb-2">
          Activity Type
        </label>
        <input
          id="activityType"
          :value="planDetail?.activityType || ''"
          type="text"
          readonly
          class="w-full border border-gray-200 bg-gray-100 rounded-md p-2.5 text-gray-500 cursor-not-allowed"
        />
      </div>

      <!-- Start Date -->
      <div>
        <label for="startDate" class="block text-sm font-semibold text-gray-700 mb-2">
          Start Date <span class="text-red-500">*</span>
        </label>
        <input
          id="startDate"
          v-model="formData.startDate"
          type="datetime-local"
          required
          class="w-full border border-gray-300 rounded-md p-2.5 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <!-- End Date -->
      <div>
        <label for="endDate" class="block text-sm font-semibold text-gray-700 mb-2">
          End Date <span class="text-red-500">*</span>
        </label>
        <input
          id="endDate"
          v-model="formData.endDate"
          type="datetime-local"
          required
          class="w-full border border-gray-300 rounded-md p-2.5 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <!-- Start Location -->
      <div>
        <label for="startLocation" class="block text-sm font-semibold text-gray-700 mb-2">
          Start Location <span class="text-red-500">*</span>
        </label>
        <select
          id="startLocation"
          v-model="formData.startLocation"
          required
          class="w-full border border-gray-300 rounded-md p-2.5 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="" disabled>Select start location</option>
          <option
            v-for="location in locations"
            :key="location.code"
            :value="location.name"
          >
            {{ location.name }}
          </option>
        </select>
      </div>

      <!-- End Location -->
      <div>
        <label for="endLocation" class="block text-sm font-semibold text-gray-700 mb-2">
          End Location <span class="text-red-500">*</span>
        </label>
        <select
          id="endLocation"
          v-model="formData.endLocation"
          required
          class="w-full border border-gray-300 rounded-md p-2.5 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="" disabled>Select end location</option>
          <option
            v-for="location in locations"
            :key="location.code"
            :value="location.name"
          >
            {{ location.name }}
          </option>
        </select>
      </div>

      <!-- Actions -->
      <div class="flex justify-end gap-3 pt-4">
        <button
          type="button"
          @click="handleCancel"
          class="px-5 py-2.5 bg-gray-300 hover:bg-gray-400 text-gray-800 rounded-md font-medium transition-colors"
        >
          Cancel
        </button>
        <button
          type="submit"
          :disabled="submitting"
          class="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white rounded-md font-medium transition-colors"
        >
          {{ submitting ? 'Saving...' : 'Save Changes' }}
        </button>
      </div>

      <!-- Messages -->
      <div
        v-if="submitError"
        class="mt-4 text-red-700 bg-red-100 border border-red-300 rounded-md px-4 py-3"
      >
        {{ submitError }}
      </div>
      <div
        v-if="submitSuccess"
        class="mt-4 text-green-700 bg-green-100 border border-green-300 rounded-md px-4 py-3"
      >
        {{ submitSuccess }}
      </div>
    </form>
  </div>
</template>
