<template>
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑文章' : '创建文章'"
        width="50%"
        
        @close="handleClose"
    >
        <el-form ref="formRef" :model="formData" label-width="80px" :rules="rules">
            <el-form-item label="标题" prop="title">
                <el-input v-model="formData.title" size="large" placeholder="请输入标题" maxlength="200" show-word-limit="true" clearable/>
            </el-form-item>
            <el-form-item label="所属分类" prop="categoryId">
                <el-select v-model="formData.categoryId"  placeholder="请选择分类">
                    <el-option v-for="item in props.categories" :key="item.value" :value="item.value" :label="item.label" />
                        
                    
                </el-select>
            </el-form-item>
            <el-form-item label="文章摘要" prop="summary">
                <el-input type="textarea" v-model="formData.summary"  placeholder="请输文章摘要" maxlength="1000" show-word-limit="true" clearable :row="4"/>
            </el-form-item>
            <el-form-item label="标签" prop="tags">
                <el-select v-model="formData.tagArray" placeholder="请输入文章标签" multiple filterable allow-create style="width: 100%;" >
                    <el-option v-for="item in commonTags" :key="item" :value="item" :label="item" />

                </el-select>
            </el-form-item>
            <el-form-item label="封面图片" >
                <div class="cover-upload">
                    <el-upload
                        class="cover-upload"
                        action="#"
                        :show-file-list="false"
                        :before-upload="beforeUpload"
                        :http-request="handleUploadRequest"
                        accept="image/*"
                    >
                    <div v-if="!imgUrl" class="cover-placeholder">
                        <p>点击上传封面</p>
                    </div>
                        <img v-else :src="imgUrl" class="cover-image" />
                    </el-upload>
                    <div v-if="imgUrl" class="cover-remove" >
                        <el-button type="danger" size="mini" @click="handleRemove">移除封面</el-button>
                    </div>
                </div>
            </el-form-item>
            <el-form-item label="内容" prop="content">
                <RichTextEditor 
                v-model:modelValue="formData.content"
                placeholder="请输入内容..."
                maxCharCount="5000"
                @change="handleContentChange"
                @created="handleEditorCreated"
                minHeight="400px"/>
                


            </el-form-item>
        </el-form>
        <div v-if="btnPreview">
            <h3>内容预览</h3>
            <div v-html="formData.content"></div>
        </div>
        <template #footer>
            <el-button  @click="btnPreview = !btnPreview">{{btnPreview?'隐藏预览' : '预览效果'}}</el-button>
            <el-button  @click="handleClose">取消</el-button>
            <el-button  type="primary" @click="handleSubmit" :loading="loading">{{isEdit?'更新文章':'创建文章'}}</el-button>
        </template>
    </el-dialog>
</template>

<script setup>
import { computed,reactive,ref,nextTick,watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFile,createArticle,updateArticle } from '@/api/admin'
import { fileBaseUrl } from '@/config/index.js'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { lo } from 'element-plus/es/locales.mjs'




const props = defineProps({
    modelValue:{
        type: Boolean,
        default: false
    },
    categories:{
        type: Array,
        default: []
    },
    article:{
        type: Object,
        default: null
    }
})


const emit = defineEmits(['update:modelValue','success'])

const dialogVisible = computed({
        get(){
            return props.modelValue
        },
        set(val){
            emit('update:modelValue',val)
        }
})

const isEdit = computed(() => !!props.article?.id)

//监听编辑数据
watch(()=>props.article,(newVal)=>{
    if(newVal){
        nextTick(()=>{
            Object.assign(formData,newVal)
            businessId.value = newVal.id
            imgUrl.value = fileBaseUrl + newVal.coverImage
        })
        
    }
})
const handleClose = () => {
    formRef.value.resetFields()
    businessId.value = null
    formData.tagArray = []
    handleRemove()
    emit('update:modelValue',false)
}

const formData = reactive({
    
    "title": "",
    "content": "",
    "coverImage": "",
    "categoryId": 1,
    "summary": "",
    "tags": "",
    "tagArray": [],
    "id": ""

})

const rules = reactive({
    title: [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { min: 1, max: 200, message: '长度在 1 到 200 个字符', trigger: 'blur' }
    ],
    categoryId: [
        { required: true, message: '请选择分类', trigger: 'blur' },
    ],
    content: [
        { required: true, message: '请输入内容', trigger: 'blur' },
        { min: 1, max: 5000, message: '长度在 1 到 5000 个字符', trigger: 'blur' }
    ]
})

const commonTags = [
  '情绪管理', '焦虑', '抑郁', '压力', '睡眠', 
  '冥想', '正念', '放松', '心理健康', '自我成长',
  '人际关系', '工作压力', '学习方法', '生活技巧'
]

const beforeUpload = (file) => {
    const isImage = file.type.startsWith('image/');
    const isLt5M = file.size / 1024 / 1024 < 5;

    if (!isImage) {
        ElMessage.error('上传图片只能是 JPG 格式!');
        return false;
    }
    if (!isLt5M) {
        ElMessage.error('上传图片大小不能超过 2MB!');
        return false;
    }
}

const businessId = ref(null)
const handleUploadRequest = async({ file}) => {

    businessId.value = crypto.randomUUID()
    const fileRes = await uploadFile(file,{
        businessId: businessId.value,
        businessType: 'ARTICLE',   
        businessField: 'cover'
    })


    imgUrl.value = fileBaseUrl+fileRes.filePath
    formData.coverImage = fileRes.filePath
}

const imgUrl = ref('')


const handleRemove = () => { 
    imgUrl.value = ''
    formData.coverImage = ''
}

const handleContentChange = (data) => {
    formData.content = data.html
}

const editorInstance = ref(null)
const handleEditorCreated = (editor) => {
    editorInstance.value = editor
    if(formData.content && editor){
        nextTick(() => {
            editor.setHtml(formData.content)
        })
        
    }
}
const loading = ref(false)
const formRef = ref(null)
const handleSubmit = () => {
    formRef.value.validate((valid,fields) => { 
        if(valid){
            loading.value = true
        }
        console.log(formData,'FormData')
        const submitData = {
            ...formData,
            tags: formData.tagArray.join(',')
        }
        delete submitData.tagArray

        if(!isEdit){
            submitData.id = businessId.value
            createArticle(submitData).then(res =>{
            loading.value = false
            emit('success')
        })}else{
            updateArticle(props.article.id,submitData).then(res =>{
            loading.value = false
            emit('success')
        })

        }

        
        
    })
}

const btnPreview = ref(false)
</script>

<style lang="scss" scoped>
.cover-placeholder{
    width: 200px;
    height: 120px;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #8b949e;
    background: #f6f8fa;
    display: flex;
}

.cover-image{
    width: 200px;
    height: 120px;
    display: block;
}

</style>