import axios from "axios";
import {ElMessage} from "element-plus";

const authItemName = "access_token";
//默认请求失败处理
const defaultFailure = (message, code, url) =>{
    console.warn(`请求地址:${url}, 状态码:${code}, 错误信息:${message}`);//警告格式
    ElMessage.warning(message)//出问题后弹窗
}
//默认异常处理
const defaultError = (error) => {
    console.error(error)
    ElMessage.warning('发生了一些错误，请联系管理员')
}
//Token的获取
function takeAccessToken(){
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    if(!str) return null
    const authObj = JSON.parse(str)
    // if(new Date(authObj.expire) <= new Date()) {
    //     deleteAccessToken()
    //     ElMessage.warning("登录状态已过期，请重新登录！")
    //     return null
    // }
    return authObj.token
}
//Token的保存
function storeAccessToken(token, remember, expire){
    const authObj = {token: token, expire: expire};
    const str = JSON.stringify(authObj);
    //判断是否勾选了"记住我"
    if(remember)
        localStorage.setItem(authItemName, str);
    else
        sessionStorage.setItem(authItemName, str);
    //判断是否过期
    if(authObj.expire <= new Date()){
        deleteAccessToken()
        ElMessage.warning('登录状态已过期，请重新登录')
    }
    return authObj.token;
}
//Token的删除
function deleteAccessToken(){
    localStorage.removeItem(authItemName);
    sessionStorage.removeItem(authItemName);
}
//获取请求头
function accessHeader(){
    const token = takeAccessToken();
    return token? {
        Authorization: `Bearer ${takeAccessToken()}`
    }: {}
}
//处理内部请求
function internalPost(url, data, header, success, failure = defaultFailure, error = defaultError) {
    axios.post(url, data, {headers: header}).then( ({data})=>{
        if(data.code === 200){
            success(data.data)
        }else{
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}
//处理内部请求
function internalGet(url, header, success, failure, error = defaultError) {
    axios.get(url, {headers: header}).then( ({data})=>{
        if(data.code === 200){
            success(data.data)
        }else{
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}
//自定义get,请求时带上token
function get(url, success, failure=defaultFailure){
    internalGet(url, accessHeader(), success, failure)
}
//自定义post
function post(url, data, success, failure=defaultFailure){
    internalPost(url, data, accessHeader(), success, failure)
}
//登录请求处理
function login(username, password, remember, success, failure = defaultFailure) {
    internalPost('/api/auth/login',{
        username: username,
        password: password,
    },{
        'Content-Type': 'application/x-www-form-urlencoded',    //表单发送格式
    }, (data) => {
        storeAccessToken(data.token,remember,data.expire)
        ElMessage.success(`登录成功,欢迎${username}来到我们的系统`)
        success(data)
    },failure)
}

function logout(success, failure = defaultFailure){
    get('/api/auth/logout', () => {
        deleteAccessToken()
        ElMessage.success(`退出登录成功，欢迎您再次使用`)
        success()
    }, failure)
}

function unauthorized(){//判断是否未验证
    return !takeAccessToken()
}
export {login, logout, get, post, unauthorized}