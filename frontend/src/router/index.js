import { createRouter, createWebHistory } from 'vue-router'
import FoundationLayout from '../layouts/FoundationLayout.vue'
import FoundationView from '../views/FoundationView.vue'
import CustomerListView from '../views/customer/CustomerListView.vue'
import CustomerFormView from '../views/customer/CustomerFormView.vue'
import CustomerDetailsView from '../views/customer/CustomerDetailsView.vue'

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
      {
        path: 'customers',
        name: 'customer-list',
        component: CustomerListView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'customers/new',
        name: 'customer-create',
        component: CustomerFormView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'customers/:id',
        name: 'customer-details',
        component: CustomerDetailsView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'customers/:id/edit',
        name: 'customer-edit',
        component: CustomerFormView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
