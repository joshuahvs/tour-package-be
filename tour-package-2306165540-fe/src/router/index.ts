import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ActivitiesView from '@/views/ActivitiesView.vue'
import PackagesView from '@/views/PackagesView.vue'
import CreatePackageView from '@/views/CreatePackageView.vue'
import EditPackageView from '@/views/EditPackageView.vue'
import PackageDetailView from '@/views/PackageDetailView.vue'
import CreatePlanView from '@/views/CreatePlanView.vue'
import ReportsView from '@/views/ReportsView.vue'
import AboutView from '../views/AboutView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/activities',
      name: 'activities',
      component: ActivitiesView,
    },
    {
      path: '/packages',
      name: 'packages',
      component: PackagesView,
    },
    {
      path: '/packages/create',
      name: 'create-package',
      component: CreatePackageView,
    },
    {
      path: '/packages/:id',
      name: 'package-detail',
      component: PackageDetailView,
    },
    {
      path: '/packages/:id/edit',
      name: 'edit-package',
      component: EditPackageView,
    },
    {
      path: '/packages/:id/plans/create',
      name: 'create-plan',
      component: CreatePlanView,
    },
    {
      path: '/reports',
      name: 'reports',
      component: ReportsView,
    },
    {
      path: '/about',
      name: 'about',
      component: AboutView,
    },
  ],
})

export default router
