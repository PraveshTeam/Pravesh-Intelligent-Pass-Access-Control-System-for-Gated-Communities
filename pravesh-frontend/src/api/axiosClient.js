import axios from 'axios'

// Major project talks to the API Gateway, which fans out to the microservices.
// VITE_API_BASE_URL is set at BUILD time (see Dockerfile / .env) -- on AWS this
// becomes your EC2 public IP or domain instead of localhost. Falls back to
// localhost automatically so local `npm run dev` still works unchanged.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' }
})

// Attach JWT on every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// On 401, force logout (only if the user was actually logged in)
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      const isLoginPage = window.location.pathname === '/login'
      const hasToken = localStorage.getItem('token')
      if (!isLoginPage && hasToken) {
        localStorage.clear()
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  }
)

export default api
