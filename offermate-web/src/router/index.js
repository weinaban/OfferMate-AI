import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const roleHomeMap = {
  1: '/',
  2: '/company',
  3: '/admin'
}

const routeRoleMap = {
  '/': 1,
  '/jobs': 1,
  '/resumes': 1,
  '/ai': 1,
  '/profile': 1,
  '/deliveries': 1,
  '/interviews': 1,
  '/chats': [1, 2],
  '/notifications': [1, 2, 3],
  '/seeker': 1,
  '/company': 2,
  '/admin': 3
}

export function getRoleHome(role) {
  return roleHomeMap[Number(role)] || '/login'
}

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/home/HomeView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/jobs',
    name: 'JobList',
    component: () => import('../views/jobs/JobListView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/jobs/:id',
    name: 'JobDetail',
    component: () => import('../views/jobs/JobDetailView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/resumes',
    name: 'ResumeList',
    component: () => import('../views/resumes/ResumeListView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/resumes/create',
    name: 'ResumeCreate',
    component: () => import('../views/resumes/ResumeCreateView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/resumes/edit/:id',
    name: 'ResumeEdit',
    component: () => import('../views/resumes/ResumeEditView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/resumes/:id',
    name: 'ResumeDetail',
    component: () => import('../views/resumes/ResumeDetailView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/deliveries',
    name: 'DeliveryList',
    component: () => import('../views/deliveries/DeliveryListView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/ai/job-match',
    name: 'AIJobMatch',
    component: () => import('../views/ai/AIJobMatchView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/ai/interview',
    name: 'AIInterview',
    component: () => import('../views/ai/AIInterviewView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/interviews',
    name: 'MyInterviewList',
    component: () => import('../views/interviews/MyInterviewListView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/profile/ProfileView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/chats',
    name: 'Chat',
    component: () => import('../views/chats/ChatView.vue'),
    meta: { requiresAuth: true, roles: [1, 2] }
  },
  {
    path: '/notifications',
    name: 'NotificationList',
    component: () => import('../views/notifications/NotificationListView.vue'),
    meta: { requiresAuth: true, roles: [1, 2, 3] }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/register/RegisterView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/seeker',
    name: 'SeekerHome',
    component: () => import('../views/seeker/SeekerHomeView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/company',
    name: 'CompanyDashboard',
    component: () => import('../views/company/CompanyDashboardView.vue'),
    meta: { requiresAuth: true, role: 2 }
  },
  {
    path: '/company/profile',
    name: 'CompanyProfile',
    component: () => import('../views/company/CompanyProfileView.vue'),
    meta: { requiresAuth: true, role: 2 }
  },
  {
    path: '/company/jobs/create',
    name: 'CompanyJobCreate',
    component: () => import('../views/company/CompanyJobCreateView.vue'),
    meta: { requiresAuth: true, role: 2 }
  },
  {
    path: '/company/jobs',
    name: 'CompanyJobList',
    component: () => import('../views/company/CompanyJobListView.vue'),
    meta: { requiresAuth: true, role: 2 }
  },
  {
    path: '/company/jobs/edit/:id',
    name: 'CompanyJobEdit',
    component: () => import('../views/company/CompanyJobEditView.vue'),
    meta: { requiresAuth: true, role: 2 }
  },
  {
    path: '/company/deliveries',
    name: 'CompanyDeliveryList',
    component: () => import('../views/company/CompanyDeliveryListView.vue'),
    meta: { requiresAuth: true, role: 2 }
  },
  {
    path: '/company/interviews',
    name: 'CompanyInterviewList',
    component: () => import('../views/company/CompanyInterviewListView.vue'),
    meta: { requiresAuth: true, role: 2 }
  },
  {
    path: '/company/:id',
    name: 'CompanyPublic',
    component: () => import('../views/company/CompanyPublicView.vue'),
    meta: { requiresAuth: true, role: 1 }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/admin/AdminDashboardView.vue'),
    meta: { requiresAuth: true, role: 3 }
  },
  {
    path: '/admin/users',
    name: 'AdminUserList',
    component: () => import('../views/admin/AdminUserListView.vue'),
    meta: { requiresAuth: true, role: 3 }
  },
  {
    path: '/admin/companies',
    name: 'AdminCompanyAudit',
    component: () => import('../views/admin/AdminCompanyAuditView.vue'),
    meta: { requiresAuth: true, role: 3 }
  },
  {
    path: '/admin/jobs',
    name: 'AdminJobAudit',
    component: () => import('../views/admin/AdminJobAuditView.vue'),
    meta: { requiresAuth: true, role: 3 }
  },
  {
    path: '/admin/operation-logs',
    name: 'AdminOperationLog',
    component: () => import('../views/admin/AdminOperationLogView.vue'),
    meta: { requiresAuth: true, role: 3 }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()

  if (to.meta.guestOnly && userStore.isLoggedIn) {
    return getRoleHome(userStore.userInfo.role)
  }

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }

  if (to.meta.requiresAuth) {
    const currentRole = Number(userStore.userInfo.role)
    const allowedRoles = to.meta.roles || (to.meta.role ? [Number(to.meta.role)] : routeRoleMap[to.path])
    const normalizedAllowedRoles = Array.isArray(allowedRoles) ? allowedRoles.map(Number) : [Number(allowedRoles)]

    if (normalizedAllowedRoles.length && !normalizedAllowedRoles.includes(currentRole)) {
      return getRoleHome(currentRole)
    }
  }

  return true
})

export default router
