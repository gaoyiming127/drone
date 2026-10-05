<!--
  根组件：仅承载路由出口。
  此处定义全局样式（重置默认边距、统一字体）与表格操作列样式（.col-action，保证操作按钮单行排列）。
-->
<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body, #app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}

/* 表格操作列：按钮单行排列、间距一致、垂直居中，避免按钮换行导致错位。
   class-name="col-action" 的列才生效，不影响其它列。 */
.col-action .cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  white-space: nowrap;
}
.col-action .cell .el-button {
  margin: 0;
  padding: 4px 8px;
}
</style>
