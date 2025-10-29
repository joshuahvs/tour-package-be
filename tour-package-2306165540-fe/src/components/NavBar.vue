<template>
  <header class="nav">
    <div class="container">
      <RouterLink to="/" class="brand">
        <span class="brand-text">Tour Package Management</span>
      </RouterLink>
      <nav class="links">
        <RouterLink to="/" class="link">Home</RouterLink>
        <RouterLink to="/activities" class="link">Activities</RouterLink>
        <RouterLink to="/packages" class="link">Packages</RouterLink>
        <RouterLink to="/reports" class="link">Reports</RouterLink>
      </nav>
    </div>
  </header>
</template>

<script setup lang="ts">
// No script needed; using <RouterLink> directly in the template.
</script>

<style scoped>
.nav {
  background: #4f46e5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.container {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 2rem;
  min-height: 64px;
}
.brand {
  font-weight: 700;
  font-size: 1.25rem;
  letter-spacing: 0.5px;
  color: white;
  text-decoration: none;
  white-space: nowrap;
  display: inline-block;
}
.brand-text {
  display: inline-block;
}
.links {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-shrink: 0;
}
.link {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  transition: all .2s ease;
  white-space: nowrap;
  font-weight: 500;
}
.link:hover {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}
.router-link-active {
  background: rgba(255, 255, 255, 0.25);
  color: white !important;
}
</style>
