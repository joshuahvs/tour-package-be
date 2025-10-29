<script setup lang="ts"></script>

<template>
  <section class="hero">
    <div class="container">
      <h1>Tour Package Management System</h1>
      <p>Plan activities, build packages, and track reports — all in one place.</p>
      <div class="cta">
        <RouterLink to="/packages" class="btn primary">Create Package</RouterLink>
        <RouterLink to="/activities" class="btn">Browse Activities</RouterLink>
      </div>
    </div>
  </section>

  <section class="features">
    <div class="container grid">
      <article class="card">
        <div class="icon">✈️</div>
        <h3>Activities</h3>
        <p>Create and manage tour activities with pricing and capacity.</p>
        <RouterLink to="/activities" class="btn link">Manage Activities</RouterLink>
      </article>

      <article class="card">
        <div class="icon">🧭</div>
        <h3>Packages</h3>
        <p>Combine activities into shareable packages with detailed plans.</p>
        <RouterLink to="/packages" class="btn link">Manage Packages</RouterLink>
      </article>

      <article class="card">
        <div class="icon">📊</div>
        <h3>Reports</h3>
        <p>View summaries and analytics for your plans and reservations.</p>
        <RouterLink to="/reports" class="btn link">View Reports</RouterLink>
      </article>
    </div>
  </section>
</template>

<style scoped>
.hero {
  background: radial-gradient(90% 120% at 20% 10%, #eef2ff 0%, rgba(238,242,255,0.4) 40%, rgba(255,255,255,0) 100%),
              linear-gradient(180deg, #ffffff 0%, #fafafe 100%);
  border-bottom: 1px solid #f0f0f5;
}
.hero .container {
  max-width: 1120px;
  margin: 0 auto;
  padding: 4rem 1rem 3rem;
  text-align: center;
}
h1 {
  font-size: 2rem;
  line-height: 1.2;
  margin: 0 0 0.75rem;
  color: #1f235a;
}
p {
  color: #5d5f7a;
  margin: 0 auto 1.5rem;
  max-width: 680px;
}
.cta {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
}
.btn {
  display: inline-block;
  padding: 0.65rem 1rem;
  border-radius: 10px;
  text-decoration: none;
  color: #373a5a;
  background: #f3f4fa;
  border: 1px solid #e7e8f5;
}
.btn.primary {
  background: #4f46e5;
  color: #fff;
  border-color: #4f46e5;
}
.features .container {
  max-width: 1120px;
  margin: 0 auto;
  padding: 2rem 1rem 4rem;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1rem;
}
.card {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #ececf3;
  border-radius: 16px;
  padding: 1.25rem;
  box-shadow: 0 8px 24px rgba(31, 35, 90, 0.06);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: #eef2ff;
  display: grid;
  place-items: center;
  font-size: 1.2rem;
  color: #373a5a;
}
.card h3 {
  margin: 0.25rem 0 0.25rem;
  color: #232654;
}
.link {
  align-self: flex-start;
}
</style>
