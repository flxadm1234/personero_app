<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-box">
          <el-icon :size="48" color="#fff"><DataAnalysis /></el-icon>
        </div>
        <h1>Sistema de Personeros</h1>
        <p class="subtitle">Panel Administrativo</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="Usuario"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="Contraseña"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          class="btn-login"
          :loading="loading"
          @click="handleLogin"
        >
          Iniciar Sesión
        </el-button>
      </el-form>

      <div class="login-footer">
        <span>© 2026 Sistema Electoral de Personeros</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, DataAnalysis } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: 'Ingrese su usuario', trigger: 'blur' }],
  password: [{ required: true, message: 'Ingrese su contraseña', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    await auth.login(form.username, form.password)
    ElMessage.success('Inicio de sesión exitoso')
    router.push('/')
  } catch (err) {
    if (err !== false) {
      ElMessage.error(err?.response?.data?.detail || 'Credenciales incorrectas')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #5b3c8f 0%, #7a52b3 50%, #5b3c8f 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 24px 60px rgba(91, 60, 143, 0.25);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-box {
  width: 88px;
  height: 88px;
  border-radius: 24px;
  background: linear-gradient(135deg, #5b3c8f, #7a52b3);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.login-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px;
}

.subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 14px;
}

.login-form {
  margin-bottom: 24px;
}

.btn-login {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #5b3c8f, #7a52b3);
  border: none;
  margin-top: 8px;
}

.btn-login:hover {
  opacity: 0.92;
}

.login-footer {
  text-align: center;
  color: #9ca3af;
  font-size: 12px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}
</style>
