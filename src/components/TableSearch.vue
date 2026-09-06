<template>
    <el-form ref="ruleFormRef" :model="formData">
        <el-row :gutter="24">
            <template v-for="item in formItemAttrs" :key="item.prop">
                <el-col v-bind="item.col">
                    <el-form-item :label="item.label" :prop="item.prop">
                        <component :is="isComp(item.component)" :placeholder="item.placeholder" v-model="formData[item.prop]" >
                            <template v-if="item.component === 'select'">
                            <el-option label="全部" value=""></el-option>
                            <el-option v-for="option in item.options" :key="option.value" :label="option.label" :value="option.value"></el-option>
                            </template>
                        </component>
                

                    </el-form-item>
                </el-col>
            
        </template>
        </el-row>
        <el-row>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset(ruleFormRef)">重置</el-button>
        </el-row>
        
    </el-form>
</template>

<script setup>
import {ref, reactive,computed} from 'vue'


const props = defineProps({
    formItem: {
        type: Array,
        default: () => []
    }
})

const emit = defineEmits(['search'])

const formItemAttrs = computed(()=>{
    const {formItem} = props
    formItem.forEach(item => {
        item.col = {xs:24, sm: 12, md: 8, lg: 6, xl: 4}
    })
    return formItem
})

const ruleFormRef = ref(null)


const formData = reactive({})

const isComp = (comp) => {
    return{
        input: 'el-input',
        select: 'el-select'
    }[comp]
}

const handleSearch = () => {
    emit('search', formData)
}

const handleReset = (formEL) => {
    if (!formEL) return
    formEL.resetFields()
    emit('search', formData)
}
</script>