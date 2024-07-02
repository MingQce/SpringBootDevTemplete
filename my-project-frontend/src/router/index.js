import {createRouter, createWebHistory} from 'vue-router'

const  router = createRouter({  //创建路由
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
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
        }
    ]
})

export default router   //暴露路由器