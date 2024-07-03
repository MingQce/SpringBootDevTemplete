<script setup>
import {User, Lock} from '@element-plus/icons-vue'
import router from "@/router";
import {reactive, ref} from "vue";
import {login} from '@/net'

const formRef = ref()
const form = reactive({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名' }
  ],
  password: [
    { required: true, message: '请输入密码'}
  ]
}

function userLogin(){
  formRef.value.validate((valid) => {
    if(valid){
      login(form.username, form.password, form.remember, () => {

      })
    }
  })
}
</script>

<template>
<div style="text-align: center;margin:0 20px;">
  <!-- 登录提示 -->
  <div style="margin-top: 150px">
    <div style="font-size: 25px">登录</div>
    <div style="font-size: 14px;color: gray">在进入系统前请先输入账号和密码进行登录</div>
  </div>
  <!-- 输入框 -->
  <div  style="margin-top: 50px">
    <el-form :model="form" :rules="rules" ref="formRef">
      <el-form-item prop="username">
        <el-input v-model="form.username" maxlength="10" type="text" placeholder="用户名/邮箱">
          <template #prefix>
            <el-icon><User/></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="form.password" maxlength="20" type="password" placeholder="密码">
          <template #prefix>
            <el-icon><Lock/></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <!-- 平分，一共24,12就是一半 -->
      <el-row>
        <el-col :span="12" style="text-align: left">
          <el-form-item prop="remember">
            <el-checkbox v-model="form.remember" label="记住我"/>
          </el-form-item>
        </el-col>
        <el-col :span="12" style="text-align: right">
          <el-link>忘记密码?</el-link>
        </el-col>
      </el-row>
    </el-form>
  </div>
  <div style="margin-top: 40px;">
    <el-button style="width: 270px;" type="success" plain @click="userLogin">立即登录</el-button>
  </div>
  <el-divider>
    <span style="font-size: 13px;color: gray">没有账号?</span>
  </el-divider>
  <div>
    <el-button style="width: 270px;" type="warning" plain>立即注册</el-button>
  </div>
</div>
</template>

<style scoped>

</style>