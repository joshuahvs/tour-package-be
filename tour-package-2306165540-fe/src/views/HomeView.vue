<script setup lang="ts"></script>

<template>
  <!-- Hero Section -->
  <section
    class="bg-[radial-gradient(90%_120%_at_20%_10%,#eef2ff_0%,rgba(238,242,255,0.4)_40%,rgba(255,255,255,0)_100%)] bg-gradient-to-b from-white to-[#fafafe] border-b border-[#f0f0f5]"
  >
    <div class="max-w-6xl mx-auto px-4 py-16 text-center">
      <h1 class="text-3xl font-bold text-[#1f235a] mb-3">
        Tour Package Management System
      </h1>
      <p class="text-[#5d5f7a] max-w-xl mx-auto mb-6">
        Plan activities, build packages, and track reports — all in one place.
      </p>
      <div class="flex justify-center gap-3">
        <RouterLink
          to="/packages"
          class="inline-block px-4 py-2 rounded-lg text-white bg-indigo-600 border border-indigo-600 hover:bg-indigo-700 transition"
        >
          Create Package
        </RouterLink>
        <RouterLink
          to="/activities"
          class="inline-block px-4 py-2 rounded-lg bg-[#f3f4fa] border border-[#e7e8f5] hover:bg-[#e8e9f3] transition"
        >
          Browse Activities
        </RouterLink>
      </div>
    </div>
  </section>

  <!-- Features Section -->
  <section class="max-w-6xl mx-auto px-4 py-16">
    <div
      class="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3"
    >
      <!-- Card 1 -->
      <article
        class="bg-white/90 border border-[#ececf3] rounded-2xl p-5 shadow-md flex flex-col gap-2 hover:shadow-lg transition"
      >
        <div
          class="w-10 h-10 rounded-xl bg-[#eef2ff] grid place-items-center text-lg text-[#373a5a]"
        >
          ✈️
        </div>
        <h3 class="text-lg font-semibold text-[#232654]">Activities</h3>
        <p class="text-[#5d5f7a]">
          Create and manage tour activities with pricing and capacity.
        </p>
        <RouterLink
          to="/activities"
          class="mt-1 inline-block text-indigo-600 hover:underline"
        >
          Manage Activities →
        </RouterLink>
      </article>

      <!-- Card 2 -->
      <article
        class="bg-white/90 border border-[#ececf3] rounded-2xl p-5 shadow-md flex flex-col gap-2 hover:shadow-lg transition"
      >
        <div
          class="w-10 h-10 rounded-xl bg-[#eef2ff] grid place-items-center text-lg text-[#373a5a]"
        >
          🧭
        </div>
        <h3 class="text-lg font-semibold text-[#232654]">Packages</h3>
        <p class="text-[#5d5f7a]">
          Combine activities into shareable packages with detailed plans.
        </p>
        <RouterLink
          to="/packages"
          class="mt-1 inline-block text-indigo-600 hover:underline"
        >
          Manage Packages →
        </RouterLink>
      </article>

      <!-- Card 3 -->
      <article
        class="bg-white/90 border border-[#ececf3] rounded-2xl p-5 shadow-md flex flex-col gap-2 hover:shadow-lg transition"
      >
        <div
          class="w-10 h-10 rounded-xl bg-[#eef2ff] grid place-items-center text-lg text-[#373a5a]"
        >
          📊
        </div>
        <h3 class="text-lg font-semibold text-[#232654]">Reports</h3>
        <p class="text-[#5d5f7a]">
          View summaries and analytics for your plans and reservations.
        </p>
        <RouterLink
          to="/reports"
          class="mt-1 inline-block text-indigo-600 hover:underline"
        >
          View Reports →
        </RouterLink>
      </article>
    </div>
  </section>
</template>
