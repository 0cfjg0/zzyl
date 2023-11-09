package com.zzyl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.google.common.collect.Lists;
import com.zzyl.constant.SuperConstant;
import com.zzyl.dto.DeptDto;
import com.zzyl.entity.Dept;
import com.zzyl.exception.BaseException;
import com.zzyl.mapper.DeptMapper;
import com.zzyl.mapper.PostMapper;
import com.zzyl.service.DeptService;
import com.zzyl.service.UserService;
import com.zzyl.utils.BeanConv;
import com.zzyl.utils.EmptyUtil;
import com.zzyl.utils.NoProcessing;
import com.zzyl.utils.ObjectUtil;
import com.zzyl.vo.DeptVo;
import com.zzyl.vo.TreeItemVo;
import com.zzyl.vo.TreeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description：部门表服务实现类
 */
@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    DeptMapper deptMapper;

    @Resource
    private PostMapper postMapper;

    @Autowired
    private UserService userService;

    /**
     * @Description 创建部门表
     * @param deptDto 对象信息
     * @return DeptVo
     */
    @Transactional
    @Override
//    @Caching(evict = {@CacheEvict(value = DeptCacheConstant.LIST,allEntries = true),
//            @CacheEvict(value = DeptCacheConstant.TREE,allEntries = true)})
    public Boolean createDept(DeptDto deptDto) {
        //转换deptDto为Dept
        Dept dept = BeanUtil.toBean(deptDto, Dept.class);

        //根据传递过来的父部门编号创建当前部门编号
        String deptNo = createDeptNo(dept.getParentDeptNo());
        dept.setDeptNo(deptNo);

        //保存
        int flag = deptMapper.insert(dept);
        if (flag!=1){
            throw new RuntimeException("保存部门信息出错");
        }

        //如果当前leader也是其他其他部门的负责人，则清空其他部门的leader数据
        //一个人只能是一个部门的leader
        if (ObjectUtil.isNotEmpty(deptDto.getLeaderId())) {
            //根据leader查询，如果存在，则清空
            deptMapper.clearOtherDeptLeader(deptDto.getLeaderId(),deptNo);
            //在用户表设置标识，表明当前部门的leader
            userService.updateIsLeaderByUserIdAndDeptNo(deptDto.getLeaderId(),dept.getDeptNo());
        }

        return true;
    }

    /**
     * @Description 修改部门表
     * @param deptDto 对象信息
     * @return Boolean
     */
    @Transactional
//    @Caching(evict = {@CacheEvict(value = DeptCacheConstant.LIST,allEntries = true),
//            @CacheEvict(value = DeptCacheConstant.TREE,allEntries = true)})
    @Override
    public Boolean updateDept(DeptDto deptDto) {
        //转换DeptVo为Dept
        Dept dept = BeanUtil.toBean(deptDto, Dept.class);

        //检验是否可以修改
        if (dept.getDataState().equals("1")) {
            if (hasChildByDeptId(dept.getDeptNo())) {
                throw new RuntimeException("存在下级部门,不允许禁用");
            }
            if (checkDeptExistUser(dept.getDeptNo())) {
                throw new RuntimeException("部门存在用户,不允许禁用");
            }
        }

        //修改
        int flag = deptMapper.updateByPrimaryKey(dept);
        if (flag==0){
            throw new RuntimeException("修改部门信息出错");
        }

        //如果当前leader也是其他其他部门的负责人，则清空其他部门的leader数据
        //一个人只能是一个部门的leader
        if (ObjectUtil.isNotEmpty(deptDto.getLeaderId())) {
            //根据leader查询，如果存在，则清空
            deptMapper.clearOtherDeptLeader(deptDto.getLeaderId(),deptDto.getDeptNo());
            //在用户表设置标识，表明当前部门的leader
            userService.updateIsLeaderByUserIdAndDeptNo(deptDto.getLeaderId(),dept.getDeptNo());
        }

        return true;
    }

    /**
     * @description 多条件查询部门表列表
     * @param deptDto 查询条件
     * @return: List<DeptVo>
     */
//    @Cacheable(value = DeptCacheConstant.LIST,key ="#deptDto.hashCode()")
    @Override
    public List<DeptVo> findDeptList(DeptDto deptDto) {
        List<Dept> deptList = deptMapper.selectList(deptDto);
        List<DeptVo> deptVos = BeanConv.toBeanList(deptList, DeptVo.class);
        deptVos.forEach(v -> v.setCreateDay(LocalDateTimeUtil.format(v.getCreateTime(), "yyyy-MM-dd")));
        return deptVos;
    }


    @Override
    public List<DeptVo> findDeptInDeptNos(List<String> deptNos) {
        List<Dept> depts = deptMapper.findDeptInDeptNos(deptNos);
        return BeanConv.toBeanList(depts,DeptVo.class);
    }


    @Override
    public String createDeptNo(String parentDeptNo) {
        if (NoProcessing.processString(parentDeptNo).length()/3==5) {
            throw new BaseException("部门最多4级");
        }
        DeptDto deptDto = DeptDto.builder().parentDeptNo(parentDeptNo).build();
        List<Dept> deptList = deptMapper.selectList(deptDto);
        //无下属节点则创建下属节点
        if (EmptyUtil.isNullOrEmpty(deptList)){
            return NoProcessing.createNo(parentDeptNo,false);
        //有下属节点则累加下属节点
        }else {
            Long deptNo = deptList.stream()
                .map(dept -> { return Long.valueOf(dept.getDeptNo());})
                .max(Comparator.comparing(i -> i)).get();
            return NoProcessing.createNo(String.valueOf(deptNo),true);
        }
    }

    @Override
    public List<DeptVo> findDeptVoListInRoleId(List<Long> roleIdSet) {
        return deptMapper.findDeptVoListInRoleId(roleIdSet);
    }

    @Transactional
//    @Caching(evict = {@CacheEvict(value = DeptCacheConstant.LIST,allEntries = true),
//            @CacheEvict(value = DeptCacheConstant.TREE,allEntries = true)})
    @Override
    public int deleteDeptById(String deptId) {
        if (hasChildByDeptId(deptId)) {
            throw new RuntimeException("存在下级部门,不允许删除");
        }
        if (checkDeptExistUser(deptId)) {
            throw new RuntimeException("部门存在用户,不允许删除");
        }

        postMapper.deletePostByDepId(deptId);
        return deptMapper.deleteDeptById(deptId);
    }

    /**
     * 启用-禁用部门
     * @param deptDto
     * @return
     */
//    @Caching(evict = {@CacheEvict(value = DeptCacheConstant.LIST,allEntries = true),
//            @CacheEvict(value = DeptCacheConstant.TREE,allEntries = true)})
    @Override
    public Boolean isEnable(DeptDto deptDto) {

        //查询部门
        Dept dept = deptMapper.selectByPrimaryKey(deptDto.getId());
        if(dept == null){
            throw new BaseException("部门不存在");
        }
        //设置状态
        dept.setDataState(deptDto.getDataState());
        //修改
        int count = deptMapper.updateByPrimaryKey(dept);
        if (count==0){
            throw new RuntimeException("修改部门信息出错");
        }
        return true;
    }

    /**
     * 是否存在子节点
     *
     * @param deptId 部门ID
     * @return 结果
     */
    public boolean hasChildByDeptId(String deptId) {
        int result = deptMapper.hasChildByDeptId(deptId);
        return result > 0 ? true : false;
    }

    /**
     * 查询部门是否存在用户
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    public boolean checkDeptExistUser(String deptId) {
        int result = deptMapper.checkDeptExistUser(deptId);
        return result > 0 ? true : false;
    }

    /**
     * 组织部门树形
     * @param deptDto 根节点
     * @return: deptDto
     */
    @Override
//    @Cacheable(value = DeptCacheConstant.TREE,key ="#deptDto.level")
    public TreeVo deptTreeVo(DeptDto deptDto) {
        //根节点查询树形结构
        String parentDeptNo = SuperConstant.ROOT_DEPT_PARENT_ID;
        List<Dept> deptList = Lists.newLinkedList();
        //指定节点查询树形结构
        DeptDto dept = DeptDto.builder()
                .dataState(SuperConstant.DATA_STATE_0)
                .parentDeptNo(NoProcessing.processString(parentDeptNo))
                .build();
        deptList.addAll(deptMapper.selectList(dept));
        if (EmptyUtil.isNullOrEmpty(deptList)){
            throw new RuntimeException("部门信息为定义！");
        }
        List<TreeItemVo> treeItemVoList  = new ArrayList<>();
        List<String> expandedIds = new ArrayList<>();
        //递归构建树形结构
        List<String> checkedDeptNoList = Lists.newArrayList();

        //找根节点
        Dept rootDept =  deptList.stream()
                .filter(d -> SuperConstant.ROOT_DEPT_PARENT_ID.equals(d.getParentDeptNo()))
                .collect(Collectors.toList()).get(0);
        recursionTreeItem(treeItemVoList,rootDept,deptList,checkedDeptNoList,expandedIds, deptDto.getLevel());
        return TreeVo.builder()
                .items(treeItemVoList)
                .checkedIds(checkedDeptNoList)
                .expandedIds(expandedIds)
                .build();
    }

    /**
     * 构建树形结构，递归调用
     * @param treeItemVoList
     * @param deptRoot
     * @param deptList
     * @param checkedDeptNos
     * @param expandedIds
     * @param level
     */
    private void recursionTreeItem(List<TreeItemVo> treeItemVoList, Dept deptRoot, List<Dept> deptList,
                                   List<String> checkedDeptNos, List<String> expandedIds, Integer level) {
        TreeItemVo treeItem = TreeItemVo.builder()
                .id(deptRoot.getDeptNo())
                .label(deptRoot.getDeptName())
                .build();
        //判断是否选择
        if (!EmptyUtil.isNullOrEmpty(checkedDeptNos)&&checkedDeptNos.contains(deptRoot.getDeptNo())){
            treeItem.setIsChecked(true);
        }else {
            treeItem.setIsChecked(false);
        }
        //是否默认展开:如果当前的部门为第二层或者第三层则展开
        if(NoProcessing.processString(deptRoot.getDeptNo()).length()/3==3){
            expandedIds.add(deptRoot.getDeptNo());
        }

        if(NoProcessing.processString(deptRoot.getDeptNo()).length()/3== level + 2){
            return;
        }
        //获得当前部门下子部门
        List<Dept> childrenDept = deptList.stream()
                .filter(n -> n.getParentDeptNo().equals(deptRoot.getDeptNo()))
                .collect(Collectors.toList());
        if (!EmptyUtil.isNullOrEmpty(childrenDept)){
            List<TreeItemVo> listChildren  = Lists.newArrayList();
            childrenDept.forEach(n->{
                this.recursionTreeItem(listChildren,n,deptList,checkedDeptNos,expandedIds, level);});
            treeItem.setChildren(listChildren);
        }
        treeItemVoList.add(treeItem);
    }
}
