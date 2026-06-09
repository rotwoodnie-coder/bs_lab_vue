<template>
  <div class="material-step">
    <div class="video-step__toolbar">
      <div class="video-step__title">实验材料</div>
      <div class="video-step__actions">
        <el-button type="primary" @click="openMaterialPicker"
          >从材料库选择材料</el-button
        >
        <el-button @click="openCreateMaterialDialog">增加材料</el-button>
      </div>
    </div>

    <div v-if="expMaterialList.length" class="material-grid">
      <div
        v-for="(item, index) in expMaterialList"
        :key="item.expMaterialId || item.materialId || index"
        class="material-card"
      >
        <div
          class="material-card__preview"
          @click="previewMaterialImage(item.mainPicUrl)"
        >
          <el-image
            v-if="resolveFileUrl(item.mainPicUrl)"
            :src="resolveFileUrl(item.mainPicPreviewUrl)"
            fit="contain"
            class="material-card__image"
          />
          <div v-else class="material-card__empty">无主图</div>
        </div>
        <div class="material-card__body">
          <div class="material-card__name">
            {{ item.materialName || "未命名材料" }}
          </div>
          <div class="material-card__tag">
            <el-input
              v-model="item.materialNum"
              placeholder="请输入用量"
              size="small"
              @blur="updateMaterialNum(index)"
            />
          </div>
        </div>
        <div class="material-card__actions">
          <el-button link type="primary" @click="showMaterialPics(item)"
            >图片</el-button
          >
          <el-button link type="danger" @click="removeMaterial(index)"
            >删除</el-button
          >
        </div>
      </div>
    </div>
    <div v-else class="step-placeholder">请从材料库选择实验材料</div>

    <el-dialog
      v-model="materialPickerVisible"
      title="选择实验材料"
      width="1100px"
    >
      <div class="picker-toolbar">
        <el-input
          v-model="materialPickerQuery.keyword"
          placeholder="搜索材料名称/用途"
          clearable
          style="width: 260px"
          @clear="handleMaterialPickerSearch"
          @keyup.enter="handleMaterialPickerSearch"
        />
        <el-button type="primary" @click="handleMaterialPickerSearch"
          >查询</el-button
        >
      </div>
      <div v-loading="materialPickerLoading" class="picker-grid">
        <div
          v-for="(item, index) in materialMaterialTable"
          :key="item.materialId || index"
          class="picker-card"
        >
          <div class="picker-card__preview">
            <el-image
              v-if="resolveFileUrl(item.mainPicUrl)"
              :src="resolveFileUrl(item.mainPicPreviewUrl)"
              fit="contain"
              class="picker-card__image"
            />
            <div v-else class="material-card__empty">无主图</div>
          </div>
          <div class="picker-card__body">
            <div class="picker-card__name" :title="item.materialName || ''">
              {{ item.materialName || "未命名材料" }}
            </div>
            <div class="picker-card__tag" :title="item.expPurpose || ''">
              {{ item.expPurpose || "无用途说明" }}
            </div>
            <div class="picker-card__actions">
              <el-button
                :type="selectedMaterialIds.includes(String(item.materialId || index)) ? 'success' : 'primary'"
                @click="toggleMaterialSelection(item)"
              >
                {{ selectedMaterialIds.includes(String(item.materialId || index)) ? '已选择' : '选择' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <div class="picker-footer">
        <div class="picker-footer__summary">共 {{ materialPickerQuery.total }} 条</div>
        <el-pagination
          background
          layout="prev, pager, next"
          :total="materialPickerQuery.total"
          :current-page="materialPickerQuery.pageNum"
          :page-size="materialPickerQuery.pageSize"
          @current-change="handleMaterialPickerPageChange"
        />
      </div>
      <template #footer>
        <el-button @click="materialPickerVisible = false">关闭</el-button>
        <el-button type="primary" :disabled="!selectedMaterialIds.length" @click="confirmMaterialSelection">选择</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="materialCreateVisible"
      title="增加材料"
      width="960px"
      class="user-dialog"
      @closed="handleMaterialCreateClosed"
    >
      <el-form
        ref="materialCreateFormRef"
        :model="materialCreateForm"
        :rules="materialCreateRules"
        label-width="120px"
        class="user-form"
      >
        <div class="material-form-layout">
          <div class="material-form-left">
            <div class="material-form-section-title">图片管理</div>
            <div class="material-pic-grid">
              <div
                v-for="(pic, index) in materialCreateForm.pics"
                :key="pic.uid"
                class="material-pic-card"
                :class="{
                  'is-main': materialCreateForm.mainPicIndex === index,
                }"
              >
                <div class="material-pic-card-top">
                  <el-tag
                    size="small"
                    :type="
                      materialCreateForm.mainPicIndex === index? 'success': 'info'"
                    >{{
                      materialCreateForm.mainPicIndex === index? "主图": `图片 ${index + 1}`
                    }}</el-tag
                  >
                  <el-button
                    link
                    type="danger"
                    :disabled="materialCreateForm.pics.length === 1"
                    @click="removeMaterialCreatePic(index)"
                    >删除</el-button
                  >
                </div>
                <MinioUploader
                  v-model="pic.url"
                  v-model:file-name="pic.name"
                  accept=".png,.jpg,.jpeg,.webp,.gif"
                  button-text="上传图片"
                  :max-size="50 * 1024 * 1024"
                  @update:modelValue="
                    handleMaterialCreatePicUploaded(index, $event)
                  "
                  @update:fileName="
                    handleMaterialCreatePicNameUpdated(index, $event)
                  "
                />
                <el-radio
                  :model-value="materialCreateForm.mainPicIndex"
                  :value="index"
                  class="material-main-radio"
                  @change="materialCreateForm.mainPicIndex = index"
                  >设为主图</el-radio
                >
              </div>
              <el-button
                class="material-add-pic-card"
                type="primary"
                plain
                @click="addMaterialCreatePic"
                >+ 添加图片</el-button
              >
            </div>
          </div>
          <div class="material-form-right">
            <el-row :gutter="16">
              <el-col :span="12"
                ><el-form-item label="材料名称" prop="materialName"
                  ><el-input
                    v-model="materialCreateForm.materialName"
                    placeholder="请输入材料名称" :maxlength="30" /></el-form-item
              ></el-col>
              <el-col :span="12"
                ><el-form-item label="材料属性" prop="materialPropId"
                  ><el-select
                    v-model="materialCreateForm.materialPropId"
                    placeholder="请选择材料属性"
                    style="width: 100%"
                    clearable
                    filterable
                    ><el-option
                      v-for="item in materialPropOptions"
                      :key="item.id"
                      :label="item.label"
                      :value="item.id" /></el-select></el-form-item
              ></el-col>
              <el-col :span="12"
                ><el-form-item label="材料分类" prop="materialTypeId"
                  ><el-select
                    v-model="materialCreateForm.materialTypeId"
                    placeholder="请选择材料分类"
                    style="width: 100%"
                    clearable
                    filterable
                    ><el-option
                      v-for="item in materialTypeOptions"
                      :key="item.id"
                      :label="item.label"
                      :value="item.id" /></el-select></el-form-item
              ></el-col>
              <el-col :span="12"
                ><el-form-item label="建议用量" prop="materialNum"
                  ><el-input
                    v-model="materialCreateForm.materialNum"
                    placeholder="如 500 毫升" :maxlength="30" /></el-form-item
              ></el-col>
              <el-col :span="12"
                ><el-form-item label="实验用途" prop="expPurpose"
                  ><el-input
                    v-model="materialCreateForm.expPurpose"
                    placeholder="请输入实验用途" :maxlength="50" /></el-form-item
              ></el-col>
              <el-col :span="12"
                ><el-form-item label="状态" prop="status"
                  ><el-radio-group v-model="materialCreateForm.status"
                    ><el-radio value="y">启用</el-radio
                    ><el-radio value="n">停用</el-radio></el-radio-group
                  ></el-form-item
                ></el-col
              >
              <el-col :span="24"
                ><el-form-item label="安全说明" prop="securityComments"
                  ><el-select
                    v-model="materialCreateSecurityValues"
                    placeholder="请选择或输入安全说明"
                    style="width: 100%"
                    multiple
                    filterable
                    allow-create
                    default-first-option
                    clearable
                    @change="handleMaterialCreateSecurityChange"
                    ><el-option
                      v-for="item in materialSecurityOptions"
                      :key="item.id"
                      :label="item.label"
                      :value="item.label" /></el-select></el-form-item
              ></el-col>
              <el-col :span="24"
                ><el-form-item label="补充说明" prop="additionalComments"
                  ><el-input
                    v-model="materialCreateForm.additionalComments"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入补充说明" /></el-form-item
              ></el-col>
            </el-row>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="materialCreateVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="materialCreateSubmitting"
          @click="handleMaterialCreateSubmit"
          >确定</el-button
        >
      </template>
    </el-dialog>

    <el-dialog
      v-model="materialPicDialogVisible"
      title="材料图片"
      width="960px"
    >
      <div v-if="materialPicList.length" class="material-pic-viewer">
        <div class="material-pic-viewer__main">
          <el-image
            :src="
              resolveFileUrl(
                materialPicList[materialPicCurrentIndex]?.materialUrl ||
                  materialPicList[materialPicCurrentIndex]?.url ||
                  '',
              )
            "
            fit="contain"
            class="material-pic-viewer__image"
            :preview-src-list="materialPicPreviewList"
            :initial-index="materialPicCurrentIndex"
            preview-teleported
          />
        </div>
      </div>
      <div v-else class="step-placeholder">暂无图片</div>
      <template #footer
        ><el-button @click="materialPicDialogVisible = false"
          >关闭</el-button
        ></template
      >
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import MinioUploader from "../../../components/MinioUploader.vue";
import {
  createMaterialMsg,
  fetchDataDictItems,
  fetchMaterialMsgs,
  fetchMaterialMsg,
} from "../../../api/index";
import {
  fetchExpMaterialPics,
  fetchExpMaterials,
  saveExpMaterial,
  updateExpMaterial,
  saveExpMaterials,
  deleteExpMaterial,
} from "../../../api/exp";

const props = defineProps({
  expId: { type: [String, Number], required: true },
});

const expMaterialList = ref([]);
const materialPickerVisible = ref(false);
const materialPickerLoading = ref(false);
const materialMaterialTable = ref([]);
const materialPickerQuery = reactive({ keyword: "", pageNum: 1, pageSize: 12, total: 0 });
const selectedMaterialIds = ref([]);
const materialCreateSubmitting = ref(false);
const materialCreateVisible = ref(false);
const materialCreateSecurityValues = ref([]);
const materialCreateFormRef = ref();
const materialPropOptions = ref([]);
const materialTypeOptions = ref([]);
const materialSecurityOptions = ref([]);
const materialPicDialogVisible = ref(false);
const materialPicList = ref([]);
const materialPicCurrentIndex = ref(0);

const createMaterialCreatePic = (data = {}) => ({
  uid:
    data.uid || `pic-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
  url: data.url || "",
  name: data.name || "",
});
const materialCreateForm = reactive({
  materialId: "",
  materialName: "",
  materialPropId: "",
  materialTypeId: "",
  materialNum: "",
  pics: [createMaterialCreatePic()],
  mainPicIndex: 0,
  expPurpose: "",
  securityComments: "",
  additionalComments: "",
  status: "y",
  isPublic: "n",
});
const materialCreateRules = {
  materialName: [
    { required: true, message: "请输入材料名称", trigger: "blur" },
  ],
  materialPropId: [
    { required: true, message: "请选择材料属性", trigger: "change" },
  ],
  materialTypeId: [
    { required: true, message: "请选择材料分类", trigger: "change" },
  ],
  materialNum: [{ required: true, message: "请输入建议用量", trigger: "blur" }],
  expPurpose: [{ required: true, message: "请输入实验用途", trigger: "blur" }],
  securityComments: [
    { required: true, message: "请输入安全说明", trigger: "blur" },
  ],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
  isPublic: [{ required: true, message: "请选择是否公开", trigger: "change" }],
};
const materialCreatePreviewSrcList = computed(() =>
  materialCreateForm.pics.map((pic) =>  resolveFileUrl(pic.url)).filter(Boolean),
);
const materialPicPreviewList = computed(() =>
  materialPicList.value
    .map((pic) => resolveFileUrl(pic.materialUrl || pic.url || ""))
    .filter(Boolean),
);
const fileUrlPrefix = import.meta.env.VITE_File_URL_PREFIX || ''
const resolveFileUrl = (value) => {
  if (!value) return ''
  //console.log(value)
  const raw = String(value).trim()
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('/')) return `${fileUrlPrefix}${raw}`
  return `${fileUrlPrefix}/${raw}`
};
const loadDicts = async () => {
  const [propRes, typeRes, secRes] = await Promise.all([
    fetchDataDictItems("data_material_prop"),
    fetchDataDictItems("data_material_type"),
    fetchDataDictItems("data_material_security"),
  ]);
  if (propRes.data.code === 200)
    materialPropOptions.value = (propRes.data.data || [])
      .map((i) => ({
        id: i.prop_id || i.id,
        label: i.prop_name || i.name || i.id,
      }))
      .filter((i) => i.id);
  if (typeRes.data.code === 200)
    materialTypeOptions.value = (typeRes.data.data || [])
      .map((i) => ({
        id: i.type_id || i.id,
        label: i.type_name || i.name || i.id,
      }))
      .filter((i) => i.id);
  if (secRes.data.code === 200)
    materialSecurityOptions.value = (secRes.data.data || [])
      .filter((i) => String(i.status || "").toLowerCase() === "y")
      .map((i) => ({
        id: i.security_id || i.id,
        label: i.security_name || i.name || i.id,
      }))
      .filter((i) => i.id);
};
const loadMaterials = async () => {
  if (!props.expId) return;
  const res = await fetchExpMaterials(props.expId);
  if (res.data.code === 200)
    expMaterialList.value = Array.isArray(res.data.data) ? res.data.data : [];
};
const openMaterialPicker = async () => {
  materialPickerVisible.value = true;
  selectedMaterialIds.value = [];
  materialPickerQuery.pageNum = 1;
  await loadMaterialMaterials();
};
const loadMaterialMaterials = async () => {
  materialPickerLoading.value = true;
  try {
    const res = await fetchMaterialMsgs({
      keyword: materialPickerQuery.keyword,
      pageNum: materialPickerQuery.pageNum,
      pageSize: materialPickerQuery.pageSize,
      status: "y",
    });
    if (res.data.code === 200) {
      const data = res.data.data || {};
      materialMaterialTable.value = Array.isArray(data) ? data : (data.records || data.list || []);
      materialPickerQuery.total = Number(data.total || materialMaterialTable.value.length || 0);
    } else {
      materialMaterialTable.value = [];
      materialPickerQuery.total = 0;
    }
  } finally {
    materialPickerLoading.value = false;
  }
};
const handleMaterialPickerSearch = async () => {
  materialPickerQuery.pageNum = 1;
  await loadMaterialMaterials();
};
const handleMaterialPickerPageChange = async (page) => {
  materialPickerQuery.pageNum = page;
  await loadMaterialMaterials();
};
const openCreateMaterialDialog = () => {
  materialCreateVisible.value = true;
};
const handleMaterialCreateClosed = () => {
  materialCreateForm.pics = [createMaterialCreatePic()];
  materialCreateForm.mainPicIndex = 0;
  materialCreateForm.materialName = "";
  materialCreateForm.materialPropId = "";
  materialCreateForm.materialTypeId = "";
  materialCreateForm.materialNum = "";
  materialCreateForm.expPurpose = "";
  materialCreateForm.securityComments = "";
  materialCreateSecurityValues.value = [];
  materialCreateForm.additionalComments = "";
  materialCreateForm.status = "y";
  materialCreateForm.isPublic = "n";
};
const addMaterialCreatePic = () =>
  materialCreateForm.pics.push(createMaterialCreatePic());
const removeMaterialCreatePic = (index) => {
  if (materialCreateForm.pics.length <= 1) return;
  materialCreateForm.pics.splice(index, 1);
};
const handleMaterialCreatePicUploaded = (index, fileUrl) => {
  const t = materialCreateForm.pics[index];
  if (t) t.url = fileUrl || "";
};
const handleMaterialCreatePicNameUpdated = (index, fileName) => {
  const t = materialCreateForm.pics[index];
  if (t) t.name = fileName || "";
};
const handleMaterialCreateSecurityChange = (values) => {
  materialCreateForm.securityComments = Array.from(
    new Set((values || []).map((i) => String(i || "").trim()).filter(Boolean)),
  ).join(";");
};
const buildMaterialCreatePayload = () => {
  const pics = materialCreateForm.pics
    .filter((pic) => pic.url)
    .map((pic, index) => ({
      url: pic.url,
      name: pic.name || "",
      sortOrder: index + 1,
    }));
  const mainPic = pics[materialCreateForm.mainPicIndex] ||
    pics[0] || { url: "" };
  return {
    ...materialCreateForm,
    mainPicUrl: mainPic.url || "",
    pics,
    materialPics: pics,
  };
};
const handleMaterialCreateSubmit = async () => {
  try {
    await materialCreateFormRef.value?.validate();
    handleMaterialCreateSecurityChange(materialCreateSecurityValues.value);
    if (!materialCreateForm.pics.some((pic) => pic.url))
      return ElMessage.warning("请至少上传一张图片");
    materialCreateSubmitting.value = true;
    const res = await createMaterialMsg(buildMaterialCreatePayload());
    if (res.data.code === 200) {
      const materialId = res.data.data?.materialId || res.data.data?.id || res.data.data || "";
      if (materialId) {
        const newMaterial = {
          expMaterialId: "",
          materialId,
          materialName: materialCreateForm.materialName || "",
          materialPropId: materialCreateForm.materialPropId || "",
          materialTypeId: materialCreateForm.materialTypeId || "",
          materialNum: materialCreateForm.materialNum || "",
          mainPicUrl: materialCreateForm.mainPicUrl || "",
          expPurpose: materialCreateForm.expPurpose || "",
          securityComments: materialCreateForm.securityComments || "",
          additionalComments: materialCreateForm.additionalComments || "",
          sortOrder: expMaterialList.value.length + 1,
          pics: materialCreateForm.pics
            .filter((pic) => pic.url)
            .map((pic, index) => ({
              url: pic.url,
              name: pic.name || "",
              sortOrder: index + 1,
            })),
        };
        newMaterial.mainPicUrl = res.data.data.mainPicUrl
        const saveRes = await saveExpMaterial(props.expId, newMaterial);
        //const expMaterialId = saveRes.data?.data?.expMaterialId || saveRes.data?.data?.id || saveRes.data?.data || "";
        //console.log(materialCreateForm.mainPicUrl)
        //console.log(newMaterial.mainPicUrl)
        //console.log(expMaterialId)
        //if (expMaterialId) newMaterial.expMaterialId = expMaterialId;
        //expMaterialList.value.push(newMaterial);
        await loadMaterials()
        materialCreateVisible.value = false;
      }
    }
  } finally {
    materialCreateSubmitting.value = false;
  }
};
const previewMaterialImage = (url) => {
  const resolved = resolveFileUrl(url || "");
  if (!resolved) return;
  materialPicList.value = [{ materialUrl: resolved }];
  materialPicCurrentIndex.value = 0;
  materialPicDialogVisible.value = true;
};
const showMaterialPics = async (item) => {
  try {
    //console.log(item);
    //console.log(item.expMaterialId);
    const res = await fetchExpMaterialPics(item?.expMaterialId || "");
    materialPicList.value = Array.isArray(res.data?.data) ? res.data.data : [];
    materialPicCurrentIndex.value = 0;
    materialPicDialogVisible.value = true;
  } catch {
    materialPicList.value = [];
    materialPicCurrentIndex.value = 0;
    materialPicDialogVisible.value = true;
  }
};
const toggleMaterialSelection = (row) => {
  const materialId = String(row.materialId || "");
  if (!materialId) return;
  const idx = selectedMaterialIds.value.indexOf(materialId);
  if (idx >= 0) selectedMaterialIds.value.splice(idx, 1);
  else selectedMaterialIds.value.push(materialId);
};

const confirmMaterialSelection = async () => {
  if (!selectedMaterialIds.value.length) {
    ElMessage.warning("请至少选择一个材料");
    return;
  }
  const selectedRows = materialMaterialTable.value.filter((row) =>
    selectedMaterialIds.value.includes(String(row.materialId || "")),
  );
  if (!selectedRows.length) {
    ElMessage.warning("请至少选择一个材料");
    return;
  }
  const existedIds = new Set(expMaterialList.value.map((item) => String(item.materialId || "")));
  const newRows = selectedRows.filter((row) => !existedIds.has(String(row.materialId || "")));
  if (!newRows.length) {
    ElMessage.warning("选择的材料已全部添加");
    return;
  }
  for (const row of newRows) {
    const newMaterial = {
      expMaterialId: "",
      materialId: row.materialId || "",
      materialName: row.materialName || "",
      materialPropId: row.materialPropId || "",
      materialTypeId: row.materialTypeId || "",
      materialNum: row.materialNum || "",
      mainPicUrl: row.mainPicUrl || "",
      expPurpose: row.expPurpose || "",
      securityComments: row.securityComments || "",
      additionalComments: row.additionalComments || "",
      sortOrder: expMaterialList.value.length + 1,
      pics: Array.isArray(row.pics) ? row.pics : [],
    };
    await saveExpMaterial(props.expId, newMaterial);
    //expMaterialList.value.push(newMaterial);
    await loadMaterials();
  }
  materialPickerVisible.value = false;
  selectedMaterialIds.value = [];
};
const removeMaterial = async (index) => {
  const item = expMaterialList.value[index];
  if (!item) return;
  try {
    await ElMessageBox.confirm(`确定删除材料【${item.materialName || `材料 ${index + 1}`}】吗？`, '提示', { type: 'warning' });
    expMaterialList.value.splice(index, 1);
    if (item.expMaterialId) await deleteExpMaterial(item.expMaterialId);
    else await saveExpMaterials(props.expId, expMaterialList.value);
    ElMessage.success('实验材料已删除');
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.response?.data?.message || error?.message || '删除失败');
    }
  }
};
const updateMaterialNum = async (index) => {
  const item = expMaterialList.value[index];
  if (!item) return;
  if (item.expMaterialId)
    await updateExpMaterial(item.expMaterialId, {
      materialNum: item.materialNum,
    });
  else await saveExpMaterials(props.expId, expMaterialList.value);
};

onMounted(async () => {
  await loadDicts();
  await loadMaterials();
});
</script>
<style scoped src="../css/ExpStandardCreateView.css"></style>