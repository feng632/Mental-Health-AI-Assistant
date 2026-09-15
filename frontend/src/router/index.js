import {createRouter, createWebHistory} from 'vue-router'
import BackendLayout from '@/components/BackendLayout.vue'
import AuthLayout from '@/components/AuthLayout.vue'
import FrontendLayout from '@/components/FrontendLayout.vue'

const backendRoutes = [
    {
        path: '/back',
        redirect: '/back/dashboard',
        component: BackendLayout,
        children: [
            {
                path: 'dashboard',
                component: () => import('@/views/dashboard.vue'),
                meta: {
                    title: '分析统计',
                    icon: 'PieChart',
                },
            },
            {
                path: 'knowledge',
                component: () => import('@/views/knowledge.vue'),
                meta: {
                    title: '知识文章',
                    icon: 'ChatLineSquare',
                }
            },
            {
                path: 'consultation',
                component: () => import('@/views/consultations.vue'),
                meta: {
                    title: '咨询服务',
                    icon: 'Message',
                }
            },
            {
                path: 'emotional',
                component: () => import('@/views/emotional.vue'),
                meta: {
                    title: '情绪日志',
                    icon: 'User',
                }
            }

        ]
    },
    {
        path: '/auth',
        component: AuthLayout,
        children: [
            {
                path: 'login',
                component: () => import('@/views/login.vue'),
                meta:{
                    title: '登录'
                }
            },
            {
                path: 'register',
                component: () => import('@/views/register.vue'),
                meta:{
                    title: '注册'
                }
            }
        ]
    }
]

const frontendRoutes = [
    {
        path: '/',
        component: FrontendLayout,
        children: [
            {
                path: '/',
                component: () => import('@/views/home.vue'),
                meta:{
                    title: '首页'
                }
            },
            {
                path: 'consultation',
                component: () => import('@/views/consultation.vue'),
                meta:{
                    title: 'AI咨询'
                }
            },
            {
                path: 'emotion-diary',
                component: () => import('@/views/emotion-diary.vue'),
                meta:{
                    title: '情绪日志'
                }
            },
            {
                path: 'knowledge',
                component: () => import('@/views/frontendKnowledge.vue'),
                meta:{
                    title: '知识文章'
                }
            },           
            {
                path: 'knowledge/article/:id',
                component: () => import('@/views/articleDetail.vue'),
                props: true,
                meta:{
                    title: '知识文章详情'
                }
            },
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes: [...backendRoutes,...frontendRoutes]
})

//路由前置守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (token) {
        const userInfo = localStorage.getItem('userInfo')
        if (!userInfo) {
            // token 存在但用户信息缺失，清除 token 回登录页
            localStorage.removeItem('token')
            next('/auth/login')
            return
        }
        // userType 后端可能返回字符串，统一转成数字再比较
        const userType = Number(JSON.parse(userInfo).userType)
        if (userType === 2) {
            if (to.path.startsWith('/back')) {
                next()
            } else {
                next('/back/dashboard')
            }
        } else if (userType === 1) {
            if (to.path.startsWith('/back') || to.path.startsWith('/auth')) {
                next('/')
            } else {
                next()
            }
        } else {
            next()
        }
    } else {
        if (to.path.startsWith('/back')) {
            next('/auth/login')
        } else {
            next()
        }
    }
})

export default router