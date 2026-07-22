import { createRouter, createWebHistory } from 'vue-router'
import FoundationLayout from '../layouts/FoundationLayout.vue'
import FoundationView from '../views/FoundationView.vue'

const routes = [
  {
    path: '/',
    component: FoundationLayout,
    children: [
      {
        path: '',
        name: 'foundation',
        component: FoundationView,
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
