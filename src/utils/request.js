import axios from "axios";
import { ElMessage } from 'element-plus';
import router from '@/router';;

const service = axios.create({
    baseURL: '/api',
    timeout: 5000,//请求超时时间
});

service.interceptors.request.use(
    config => {
        // 在发送请求之前做些什么
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['token'] = token;
        }
        return config;
    },
    error => {
        // 对请求错误做些什么
        return Promise.reject(error);
    }
);

service.interceptors.response.use(
    response => {
        // 对响应数据做点什么
        const{ data,config } = response;
        if(data.code === '200'){
            return data.data;
        }else{
            if(data.code === '-1'){
                if(!config.url?.includes('/login')){
                    ElMessage.error(data.msg ||'登录已过期，请重新登录');

                    localStorage.removeItem('token');
                    localStorage.removeItem('userInfo');
                    window.location.href = '/auth/login';
                }
            }else{
                ElMessage.error(data.msg ||'登录已过期，请重新登录');
                return Promise.reject('网络请求失败...');
            }
        }
        return response.data;
    },
    (error) => {
        // 对响应错误做点什么
        return Promise.reject(error);
    }
);

export default service;