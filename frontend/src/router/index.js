import { createRouter, createWebHistory } from 'vue-router'
import FoundationLayout from '../layouts/FoundationLayout.vue'
import FoundationView from '../views/FoundationView.vue'
import CustomerListView from '../views/customer/CustomerListView.vue'
import CustomerFormView from '../views/customer/CustomerFormView.vue'
import CustomerDetailsView from '../views/customer/CustomerDetailsView.vue'
import ServiceListView from '../views/service/ServiceListView.vue'
import ServiceFormView from '../views/service/ServiceFormView.vue'
import ServiceDetailsView from '../views/service/ServiceDetailsView.vue'
import EmployeeListView from '../views/schedule/EmployeeListView.vue'
import EmployeeFormView from '../views/schedule/EmployeeFormView.vue'
import AppointmentCalendarView from '../views/schedule/AppointmentCalendarView.vue'
import AppointmentListView from '../views/schedule/AppointmentListView.vue'
import AppointmentFormView from '../views/schedule/AppointmentFormView.vue'
import AppointmentDetailsView from '../views/schedule/AppointmentDetailsView.vue'
import ServiceOrderListView from '../views/serviceorder/ServiceOrderListView.vue'
import ServiceOrderFormView from '../views/serviceorder/ServiceOrderFormView.vue'
import ServiceOrderDetailsView from '../views/serviceorder/ServiceOrderDetailsView.vue'
import ServiceOrderTimelineView from '../views/serviceorder/ServiceOrderTimelineView.vue'

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
      {
        path: 'services',
        name: 'service-list',
        component: ServiceListView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'services/new',
        name: 'service-create',
        component: ServiceFormView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'services/:id',
        name: 'service-details',
        component: ServiceDetailsView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'services/:id/edit',
        name: 'service-edit',
        component: ServiceFormView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'employees',
        name: 'employee-list',
        component: EmployeeListView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'employees/new',
        name: 'employee-create',
        component: EmployeeFormView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'employees/:id/edit',
        name: 'employee-edit',
        component: EmployeeFormView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'appointments',
        name: 'appointment-calendar',
        component: AppointmentCalendarView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'appointments/list',
        name: 'appointment-list',
        component: AppointmentListView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'appointments/new',
        name: 'appointment-create',
        component: AppointmentFormView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'appointments/:id',
        name: 'appointment-details',
        component: AppointmentDetailsView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'appointments/:id/edit',
        name: 'appointment-edit',
        component: AppointmentFormView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'service-orders',
        name: 'service-order-list',
        component: ServiceOrderListView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'service-orders/new',
        name: 'service-order-create',
        component: ServiceOrderFormView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'service-orders/:id',
        name: 'service-order-details',
        component: ServiceOrderDetailsView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'service-orders/:id/edit',
        name: 'service-order-edit',
        component: ServiceOrderFormView,
        meta: {
          requiresAuth: true,
          roles: ['ADMIN', 'GESTOR', 'OPERADOR'],
        },
      },
      {
        path: 'service-orders/:id/timeline',
        name: 'service-order-timeline',
        component: ServiceOrderTimelineView,
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
