<script setup>
import {User} from "@element-plus/icons-vue"
import {reactive} from "vue";
const form = reactive({
  username:'',
  password:'',
  remember:false,
})
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
    <el-form v-model="form">
      <el-form-item>
        <el-input v-model="form.username" maxlength="10" type="text" placeholder="用户名/邮箱">
          <template #prefix>
            <el-icon><User/></el-icon>
          </template>
        </el-input>
      </el-form-item>
    </el-form>
  </div>

</div>
</template>

<style scoped>

</style>