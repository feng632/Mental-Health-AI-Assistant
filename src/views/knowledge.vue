<template>
    <div>
        <PageHead title="知识文章">
            <template #buttons>
                <el-button @click="handleEdit({})" type="primary">新增</el-button>
            </template>
        </PageHead>
        <TableSearch :formItem="formItem" @search="handleSearch"/>
        <el-table :data="tableData" style="width:100%;margin-top: 25px;">
            <el-table-column  label="文章标题" fixed="left" width="200" >
                <template #default="scope">
                    <div style="display: flex;align-items: center;">
                        <el-icon><timer /></el-icon>
                        <span>{{scope.row.title}}</span>
                    </div>
                </template>
            </el-table-column>

            <el-table-column  label="分类" width="200" >
                <template #default="scope">
                    <div style="display: flex;align-items: center;">
                        <el-icon><timer /></el-icon>
                        <span>{{scope.row.categoryName}}</span>
                    </div>
                </template>
            </el-table-column>

            <el-table-column prop="authorName" label="作者" width="150" />
            <el-table-column prop="readCount" label="阅读量" width="150" />
            <el-table-column prop="publishedAt" label="发布时间" width="150" />
            <el-table-column  label="操作" width="240" fixed="right">
                <template #default="scope">
                    <el-button text type="primary" @click="handleEdit(scope.row)">编辑</el-button>
                    <el-button @click="handlePublish(scope.row)" v-if="scope.row.status === 0 || scope.row.status === 2" text type="success">发布</el-button>
                    <el-button @click="handleUnpublish(scope.row)" v-if="scope.row.status === 1" text type="warning">下线</el-button>
                    <el-button @click="handlDelete(scope.row)" text type="danger">删除</el-button>
                </template>
            </el-table-column>

        </el-table>

        <el-pagination 
        style="margin-top: 25px;"
        :page-size="pagination.size"
        layout="prev,pager,next"
        :total="pagination.total"
        @current-change="handleChange"
        />

        <ArticleDialog v-model:modelValue="dialogVisible" :categories="categories" @success="handleSuccess" :article="currentArticle"/>
    </div>
</template>

<script setup>
import { onMounted,ref,reactive } from 'vue';
import PageHead from '@/components/PageHead.vue'
import TableSearch from '../components/TableSearch.vue';
import { categoryTree,articlePage,changeArticleStatus,deleteArticle } from '@/api/admin.js';
import ArticleDialog from '@/components/ArticleDialog.vue';
import { getArticleDetail } from '@/api/admin.js';
import { ElMessage, ElMessageBox } from 'element-plus';


const formItem = [
    {component: 'input', label: '文章标题', prop: 'title',placeholder: '请输入文章标题'},
    {component: 'select', label: '分类', prop: 'categoryId',placeholder: '请选择分类', },
    {component: 'select', label:'状态',prop:'status',placeholder:'请输入文章内容',options:[{
        label:'草稿',
        value:'0',
    },{
        label:'已发布',
        value:'1',
    },{
        label:'已下线',
        value:'2',
    }]}
]

const pagination = reactive({
    currentPage: 1,
    size: 10,
    total:0
})


const handleSearch =async (formData) => {
    const params = {
        ...pagination,
        ...formData
    }

    const {records,total} = await articlePage(params)
    tableData.value = records
    pagination.total = total
}


const handleChange = (page) =>{
    pagination.currentPage = page
    handleSearch()
}

const categories = ref([])

const tableData = ref([])

const dialogVisible = ref(false)
const currentArticle = ref()

const handleSuccess = () =>{
    dialogVisible.value = false
    handleSearch()
}

const handleEdit = (row) =>{
    if(!row.id){
        currentArticle.value = null
        dialogVisible.value = true
    }else{
        getArticleDetail(row.id).then(res =>{
        console.log(res,'编辑详情')
        currentArticle.value = res
        dialogVisible.value = true
    }
    
    )}
}

//发布
const handlePublish = (row) =>{
    ElMessageBox.confirm(
        `确认发布文章${row.title}吗？`,
        '确认',
        {
            confirmButtonText:'确认',
            cancelButtonText:'取消',
            type:'info'
        }
    ).then(() => {
        // 发布
        changeArticleStatus(row.id,{status:1}).then(() =>{
            ElMessage.success('发布成功')
            handleSearch()
        })
    }).catch(() => {
        // 取消
    });
}

//下线
const handleUnpublish = (row) =>{
    ElMessageBox.confirm(
        `确认下线文章${row.title}吗？`,
        '确认',
        {
            confirmButtonText:'确认',
            cancelButtonText:'取消',
            type:'warning'
        }
    ).then(() => {
        // 确认
        changeArticleStatus(row.id,{status:2}).then(() =>{
            ElMessage.success('下线成功')
            handleSearch()
        })
    })
}

//删除
const handlDelete = (row) =>{
    ElMessageBox.confirm(
        `确认删除文章${row.title}吗？`,
        '确认',
        {
            confirmButtonText:'确认删除',
            cancelButtonText:'取消',
            type:'danger'
        }
    ).then(() => {
        // 确认
        deleteArticle(row.id).then(() =>{
            ElMessage.success('删除成功')
            handleSearch()
        })
    })
}

onMounted(async ()=>{
    const data = await categoryTree()
    categories.value = data.map(item => ({
        label: item.categoryName,
        value: item.id
    }))
    formItem[1].options = categories.value

    handleSearch()
})

</script>