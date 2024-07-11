import {createRouter, createWebHistory} from 'vue-router'
import {unauthorized} from "@/net/index.js";

const  router = createRouter({  //创建路由
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {//登录页面
            path: '/',  //路径
            name: 'welcome',    //路由名字
            component: () => import('@/views/WelcomeView.vue'),  //绑定组件
            children: [  //子路由
                {
                    path:'',
                    name:'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue'),
                }
            ]
        },{//主页
            path:'/index',
            name:'index',
            component: () => import("@/views/indexView.vue"),

        }
    ]
})
router.beforeEach((to, from, next) => {//配置路由守卫，防止没登录的情况下直接访问主页等页面
    const isUnauthorized = unauthorized()
    if(to.name.startsWith("welcome-") && !isUnauthorized){//已经登录但还想访问welcome开头的页面
        next('/index')
    } else if(to.fullPath.startsWith('/index') && isUnauthorized){//未登录，想要访问主页
        next('/')
    }else{//正常情况
        next()
    }
})
export default router   //暴露路由器