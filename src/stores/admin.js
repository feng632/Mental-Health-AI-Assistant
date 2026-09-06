import {defineStore} from "pinia";
import {ref} from "vue";

export const useAdminStore = defineStore('admin',() => {
    const isCollapsed = ref(false)

    const toggleCollapse = () => {
        isCollapsed.value = !isCollapsed.value
    }

    return {
        isCollapsed,
        toggleCollapse
    }
})