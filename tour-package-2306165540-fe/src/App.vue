<script setup lang="ts">
import { RouterView } from 'vue-router'
import NavBar from './components/NavBar.vue'
</script>

<template>
  <NavBar />
  <main>
    <RouterView />
  </main>
</template>

<style scoped>
main {
  min-height: 100vh;
  background: linear-gradient(180deg, #f7f7fb 0%, #ffffff 40%);
}
</style>
