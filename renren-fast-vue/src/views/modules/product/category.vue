<!--  -->
<template>
  <div>
    <el-switch
      v-model="draggable"
      active-text="开启拖拽"
      inactive-text="关闭拖拽"
    ></el-switch>
    <el-button v-if="draggable" @click="batchSave">批量保存</el-button>
    <el-button type="danger" @click="batchDelete">批量删除</el-button>
    <!-- 树形选择框 -->
    <!-- 展示勾选框，设置节点主键，设置点击节点时不扩展，设置默认根据哪个key进行展开树形结构（防止删除按钮后全部菜单都缩回去），设置节点可拖拽 -->
    <el-tree
      :data="menus"
      :props="defaultProps"
      @node-click="handleNodeClick"
      show-checkbox
      node-key="catId"
      :default-expanded-keys="expandedKey"
      :expand-on-click-node="false"
      :draggable="draggable"
      :allow-drop="allowDrop"
      @node-drop="handleDrop"
      ref="menuTree"
    >
      <!-- 为树形结构列表的每一项添加自定义按钮实现删除功能 -->
      <!-- 传入的 { node, data } 为解构形式，node 为当前节点，data 为其数据 -->
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <!-- 节点的标签 -->
        <span>{{ node.label }}</span>
        <!-- 为当前节点添加两个按钮,分别绑定两个方法:添加和删除 -->
        <span>
          <!-- 一级和二级分类才显示增加按钮 -->
          <el-button
            v-if="node.level <= 2"
            type="text"
            size="mini"
            @click="() => append(data)"
          >
            Append
          </el-button>
          <!-- 所有分类都要显示编辑按钮 -->
          <el-button type="text" size="mini" @click="edit(data)"
            >edit</el-button
          >
          <!-- 三级分类才显示删除按钮 -->
          <el-button
            v-if="node.childNodes.length == 0"
            type="text"
            size="mini"
            @click="() => remove(node, data)"
          >
            Delete
          </el-button>
        </span>
      </span></el-tree
    >

    <!-- 对话框，用于新增菜单项 -->
    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form :model="category">
        <el-form-item label="分类名称">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="category.icon" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="计量单位">
          <el-input
            v-model="category.productUnit"
            autocomplete="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitData">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
//这里可以导入其他文件（比如：组件，工具js，第三方插件js，json文件，图片文件等等）
//例如：import 《组件名称》 from '《组件路径》';

export default {
  //import引入的组件需要注入到对象中才能使用
  components: {},
  data() {
    return {
      menus: [],
      expandedKey: [],
      // 对话框默认不显示
      dialogVisible: false,
      // 表单项的内容
      title: "",
      // 每个节点从数据库中读取到的信息
      category: {
        name: "",
        parentCid: 0,
        catLevel: 0,
        showStatus: 1,
        sort: 0,
        productUnit: "",
        icon: "",
        catId: null,
      },
      maxLevel: 0,
      // 设置是否可以开启拖拽功能
      draggable: false,
      dialogType: "", //edit,add
      // 父节点的id
      pCid: [],
      // 保存拖拽后新的节点信息（id，父id，新层级，新排序），是一个JSON数组，保存许多的节点JSON对象
      updateNodes: [],
      defaultProps: {
        children: "children",
        // menus里的哪个属性是要显示在界面上的
        label: "name",
      },
    };
  },
  //方法集合
  methods: {
    handleNodeClick(data) {
      console.log(data);
    },
    // 向后端发送数据，获取三级菜单信息
    getMenus() {
      this.$http({
        url: this.$http.adornUrl("/product/category/list/tree"),
        method: "get",
      }).then(({ data }) => {
        // 解构出请求里的data属性，取出里面的data数据
        console.log("成功获取到菜单数据...", data.data);
        this.menus = data.data;
      });
    },
    // ----------------------- 表单项的回调函数 -------------------------------
    // 表单的提交按钮点击之后：
    submitData() {
      if (this.dialogType === "add") {
        this.addCategory();
      }
      if (this.dialogType === "edit") {
        this.editCategory();
      }
    },
    //修改三级分类数据
    editCategory() {
      const { catId, name, icon, productUnit } = this.category;
      this.$http({
        url: this.$http.adornUrl("/product/category/update"),
        method: "post",
        data: this.$http.adornData({ catId, name, icon, productUnit }, false),
      }).then(({ data }) => {
        this.$message({
          message: "菜单修改成功",
          type: "success",
        });
        //关闭对话框
        this.dialogVisible = false;
        //刷新出新的菜单
        this.getMenus();
        //设置需要默认展开的菜单
        this.expandedKey = [this.category.parentCid];
      });
    },
    //添加三级分类
    addCategory() {
      console.log("提交的三级分类数据", this.category);
      this.$http({
        url: this.$http.adornUrl("/product/category/save"),
        method: "post",
        data: this.$http.adornData(this.category, false),
      }).then(({ data }) => {
        this.$message({
          message: "菜单保存成功",
          type: "success",
        });
        //关闭对话框
        this.dialogVisible = false;
        //刷新出新的菜单
        this.getMenus();
        //设置需要默认展开的菜单
        this.expandedKey = [this.category.parentCid];
      });
    },

    // ----------------------- 按钮的回调函数 -------------------------------
    // 为每个节点添加“增加”按钮
    // 增加按钮用于添加新的菜单项
    append(data) {
      console.log("append", data);
      this.dialogType = "add";
      this.title = "添加分类";
      // 点击新增按钮后，可视化对话框
      this.dialogVisible = true;

      // 设置菜单项内容
      this.category.parentCid = data.catId;
      this.category.catLevel = data.catLevel * 1 + 1;
      this.category.catId = null;
      this.category.name = "";
      this.category.icon = "";
      this.category.productUnit = "";
      this.category.sort = 0;
      this.category.showStatus = 1;
    },
    edit(data) {
      console.log("要修改的数据", data);
      this.dialogType = "edit";
      this.title = "修改分类";
      this.dialogVisible = true;

      //发送请求获取当前节点最新的数据
      this.$http({
        url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
        method: "get",
      }).then(({ data }) => {
        //请求成功
        console.log("要回显的数据", data);
        this.category.name = data.data.name;
        this.category.catId = data.data.catId;
        this.category.icon = data.data.icon;
        this.category.productUnit = data.data.productUnit;
        this.category.parentCid = data.data.parentCid;
        this.category.catLevel = data.data.catLevel;
        this.category.sort = data.data.sort;
        this.category.showStatus = data.data.showStatus;
        /**
         *         parentCid: 0,
         catLevel: 0,
         showStatus: 1,
         sort: 0,
         */
      });
    },
    // 为每个节点添加“删除”按钮
    remove(node, data) {
      // 获取所有数据的id
      const ids = [data.catId];
      this.$confirm(`是否删除【${data.name}】菜单?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          this.$http({
            // 发出post请求删除数据
            url: this.$http.adornUrl("/product/category/delete"),
            method: "post",
            // 指定要删除的菜单id
            data: this.$http.adornData(ids, false),
          }).then(({ data }) => {
            this.$message({
              message: "菜单删除成功",
              type: "success",
            });
            //刷新出新的菜单
            this.getMenus();
            //设置需要默认展开的菜单
            this.expandedKey = [node.parent.data.catId];
          });
        })
        .catch(() => {});

      console.log("remove", node, data);
    },
    // 判断是否允许拖拽：返回true代表可以拖拽，返回false代表不可以拖拽
    // 拖过去要保证，拖过去之后形成的新的项的深度小于等于3，如果拖到里面，就判断target的深度+source向下的深度 <=3，如果不是拖到里面（和target同级），就判断 target的父节点的深度+source向下的深度 <= 3
    allowDrop(draggingNode, dropNode, type) {
      //1、被拖动的当前节点以及所在的父节点总层数不能大于3

      //1）、被拖动的当前节点总层数
      console.log("allowDrop:", draggingNode, dropNode, type);
      //
      this.countNodeLevel(draggingNode);
      //当前正在拖动的节点+父节点所在的深度不大于3即可
      let deep = Math.abs(this.maxLevel - draggingNode.level) + 1;
      console.log("深度：", deep);

      //   this.maxLevel
      if (type == "inner") {
        // console.log(
        //   `this.maxLevel：${this.maxLevel}；draggingNode.data.catLevel：${draggingNode.data.catLevel}；dropNode.level：${dropNode.level}`
        // );
        return deep + dropNode.level <= 3;
      } else {
        return deep + dropNode.parent.level <= 3;
      }
    },
    countNodeLevel(node) {
      //找到所有子节点，求出最大深度
      if (node.childNodes != null && node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          if (node.childNodes[i].level > this.maxLevel) {
            this.maxLevel = node.childNodes[i].level;
          }
          this.countNodeLevel(node.childNodes[i]);
        }
      }
    },
    // 在拖拽完成后，触发该事件，
    handleDrop(draggingNode, dropNode, dropType, ev) {
      console.log("handleDrop: ", draggingNode, dropNode, dropType);
      //1、获取当前节点最新的父节点id
      let pCid = 0;
      let siblings = null;
      // 如果不是inner，则新的父节点就是target的父节点
      if (dropType == "before" || dropType == "after") {
        pCid =
          dropNode.parent.data.catId == undefined
            ? 0
            : dropNode.parent.data.catId;
        // 新的兄弟节点为新的父亲的所有孩子
        siblings = dropNode.parent.childNodes;
      } else {
        // 如果是inner，则新的父节点就是target
        pCid = dropNode.data.catId;
        // 新的兄弟节点为新的父亲的所有孩子
        siblings = dropNode.childNodes;
      }
      this.pCid.push(pCid);

      //2、更新当前拖拽节点的最新顺序，遍历后重新排序
      for (let i = 0; i < siblings.length; i++) {
        if (siblings[i].data.catId == draggingNode.data.catId) {
          //如果遍历的是当前正在拖拽的节点，则要先更新正在拖拽的节点的父节点id等信息
          let catLevel = draggingNode.level;
          // 如果正在拖拽的节点的层级发生了变化，则递归修改其孩子的层级信息，同步后将发给数据库，将新的层级关系保存
          if (siblings[i].level != draggingNode.level) {
            //如果当前节点的层级发生变化
            catLevel = siblings[i].level;
            //修改他子节点的层级
            this.updateChildNodeLevel(siblings[i]);
          }
          //保存当前节点的新的排序信息、层级信息、父节点信息到updateNodes中，后面将统一保存到数据库
          this.updateNodes.push({
            catId: siblings[i].data.catId,
            sort: i,
            parentCid: pCid,
            catLevel: catLevel,
          });
        } else {
          // 如果遍历的不是正在拖拽的节点，则直接保存
          this.updateNodes.push({ catId: siblings[i].data.catId, sort: i });
        }
      }

      //3、当前拖拽节点的最新层级
      console.log("updateNodes", this.updateNodes);
    },
    // 递归更新所有孩子节点的层级
    updateChildNodeLevel(node) {
      if (node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          const cNode = node.childNodes[i].data;
          // 遍历node的所有孩子，将其新的层级信息保存到updateNodes中，后面将统一保存到数据库
          this.updateNodes.push({
            catId: cNode.catId,
            catLevel: node.childNodes[i].level,
          });
          this.updateChildNodeLevel(node.childNodes[i]);
        }
      }
    },
    // 点击保存时，将updateNodes中保存的节点一起更新到数据库中
    batchSave() {
      this.$http({
        url: this.$http.adornUrl("/product/category/update/sort"),
        method: "post",
        data: this.$http.adornData(this.updateNodes, false),
      }).then(({ data }) => {
        this.$message({
          message: "菜单顺序等修改成功",
          type: "success",
        });
        //刷新出新的菜单
        this.getMenus();
        //设置需要默认展开的菜单
        this.expandedKey = this.pCid;
        //每一次拖拽完成后，记得重置下面两个参数，以免前一次的拖拽影响当前的拖拽
        this.updateNodes = [];
        this.maxLevel = 0;
        // this.pCid = 0;
      });
    },
    // 批量删除
    batchDelete() {
      let catIds = [];
      let catNames = [];
      let checkedNodes = this.$refs.menuTree.getCheckedNodes();
      console.log("被选中的元素", checkedNodes);
      for (let i = 0; i < checkedNodes.length; i++) {
        catIds.push(checkedNodes[i].catId);
        catNames.push(checkedNodes[i].name);
      }
      this.$confirm(`是否批量删除【${catNames}】菜单?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          this.$http({
            url: this.$http.adornUrl("/product/category/delete"),
            method: "post",
            data: this.$http.adornData(catIds, false),
          }).then(({ data }) => {
            this.$message({
              message: "菜单批量删除成功",
              type: "success",
            });
            this.getMenus();
          });
        })
        .catch(() => {});
    },
  },

  //监听属性 类似于data概念
  computed: {},
  //监控data中的数据变化
  watch: {},

  //生命周期 - 创建完成（可以访问当前this实例）
  created() {
    // 创建组件就向后端发送数据，获取三级菜单
    this.getMenus();
  },
  //生命周期 - 挂载完成（可以访问DOM元素）
  mounted() {},
  beforeCreate() {}, //生命周期 - 创建之前
  beforeMount() {}, //生命周期 - 挂载之前
  beforeUpdate() {}, //生命周期 - 更新之前
  updated() {}, //生命周期 - 更新之后
  beforeDestroy() {}, //生命周期 - 销毁之前
  destroyed() {}, //生命周期 - 销毁完成
  activated() {}, //如果页面有keep-alive缓存功能，这个函数会触发
};
</script>
<style scoped>
</style>