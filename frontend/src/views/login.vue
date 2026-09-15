<template>
    <div class="container">
        <div class="title">
            <div class="backhome">
                <el-icon>
                    <Back />
                </el-icon>
                <span>返回首页</span>
            </div>
            <div class="title-text">
                <h2>登录您的账户</h2>
                <p>请输入您的用户名和密码</p>
            </div>
        </div>
        <div class="form-container">
            <el-form
                ref="ruleFormRef"
                :model="formData"
                label-position="top"
                class="demo-ruleForm"
                :rules="rules">

                <el-form-item label="用户名或邮箱" prop="username">
                    <el-input v-model="formData.username" size="large" placeholder="请输入用户名或邮箱"></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="formData.password" type="password" size="large" placeholder="请输入密码" show-password></el-input>
                </el-form-item>
                <el-button class="btn" size="large" type="primary" @click="submitForm(ruleFormRef)">登录</el-button>
            </el-form>
            <div class="footer">
                <p>还没有账户？<router-link to="/register">立即注册</router-link></p>
                
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
.container{
    width: 384px;
    .title{
        .backhome{
            margin-bottom: 60px;
        }
        .title-text{
            text-align: center;
            h2{
                font-size: 36px;
                margin-bottom: 10px;
            }
            p{
                font-size: 18px;
                color: #6b7280;
            }
        }
    }
    .form-container{
        margin-top: 30px;
        .btn{
            width: 100%;
            margin-top: 20px;
        }
        .footer{
            padding:30px;
            text-align: center;
        }
    }
}





</style>


<script setup>
import {ref, reactive} from 'vue'
import {login} from '@/api/admin.js'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const ruleFormRef = ref()

const formData = reactive({
    username: '',
    password: ''
})

const rules = reactive({
    username: [
        { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ]
})

const submitForm = async(formEl) => {
    if (!formEl) return
    await formEl.validate((valid) => {
        if (!valid) return

        login(formData).then(data => {
            if(!data.token){
                return console.log('登录失败')
            }

            localStorage.setItem('token', data.token)
            localStorage.setItem('userInfo', JSON.stringify(data.userInfo))

            // 根据用户角色决定跳转的路径
            // 注意：userType 后端可能返回字符串，用 Number() 统一转成数字再比较
            if(Number(data.userInfo.userType) === 2){
                router.push('/back/dashboard')
            }else{
                router.push('/')
            }
        }).catch(() => {
            ElMessage.error('登录失败，请检查账号密码')
        })
    })
}


</script>