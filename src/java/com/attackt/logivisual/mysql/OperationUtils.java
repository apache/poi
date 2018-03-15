package com.attackt.logivisual.mysql;

import com.alibaba.fastjson.JSON;
import com.attackt.logivisual.model.newfunctions.CellJsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OperationUtils {
    JdbcUtils jdbcUtils=null;
    public OperationUtils(){
        this.jdbcUtils = new JdbcUtils();
    }
    /**
     * 保存cellJson
     * @param id
     * @param cellJsonObject
     * @return
     */
    public boolean saveData(String id,CellJsonObject cellJsonObject){
        try {
            this.jdbcUtils = new JdbcUtils();
            String sql = "insert into excel_cell (excel_uid,excel_cell_id,bolan_str,content,formula,status,sheet_names,formula_type) values(?,?,?,?,?,?,?,?);";
            List<Object> params = new ArrayList<Object>();
            params.add(id);
            params.add(cellJsonObject.getCellId());
            params.add(cellJsonObject.getBolanStr());
            params.add(JSON.toJSONString(cellJsonObject.getData()));
            params.add(cellJsonObject.getFormula());
            params.add(cellJsonObject.getStatus());
            params.add(JSON.toJSONString(cellJsonObject.getSheetNames()));
            params.add(cellJsonObject.getFormulaType());
            boolean flag = this.jdbcUtils.updateByPreparedStatement(sql, params);
            return flag;
        } catch (Exception e) {
            System.out.println(e);
        }finally {
            this.jdbcUtils.releaseConnection();
        }
        return false;
    }

    /**
     * 查找CellJson并返回
     * @param excel_id
     * @return
     */
    public Map<String, Object> findData(String excel_id)
    {
        try {
            this.jdbcUtils = new JdbcUtils();
            String sql = "select * from excel_cell where excel_uid=? and status=1 order by id desc limit 0,1;";
            List<Object> params = new ArrayList<Object>();
            params.add(excel_id);
            return this.jdbcUtils.findSimpleResult(sql, params);
        } catch (Exception e) {
            System.out.println(e);
        }finally {
            this.jdbcUtils.releaseConnection();
        }
        return null;
    }

    /**
     * 更改CellJson信息
     * @param id
     * @return
     */
    public boolean updateData(int id, String content)
    {
        try {
            this.jdbcUtils = new JdbcUtils();
            String sql = "update excel_cell set status=2,content=? where id=?;";
            List<Object> params = new ArrayList<Object>();
            params.add(content);
            params.add(id);
            boolean flag = this.jdbcUtils.updateByPreparedStatement(sql, params);
            return flag;
        } catch (Exception e) {
            System.out.println(e);
        }finally {
            this.jdbcUtils.releaseConnection();
        }
        return false;
    }

    /**
     * 根据uid查询所有的记录
     * @param uid uid
     * @return 结果集 select * from excel_cell where excel_uid=1;
     */
    public List<Map<String, Object>> findUidQueryAll(String uid)
    {
        try {
            this.jdbcUtils = new JdbcUtils();
            String sql = "select * from excel_cell where excel_uid=? and status!=3;";
            List<Object> params = new ArrayList<Object>();
            params.add(uid);
            return this.jdbcUtils.findModeResult(sql, params);
        } catch (Exception e) {
            System.out.println(e);
        }finally {
            this.jdbcUtils.releaseConnection();
        }
        return null;
    }
    /**
     * 更改CellJson信息
     * @param excel_uid uid
     * @param status status
     * @return 是否执行成功
     */
    public boolean updateAllStatus(String excel_uid,int status)
    {
        try {
            this.jdbcUtils = new JdbcUtils();
            String sql = "update excel_cell set status=? where excel_uid=?;";
            List<Object> params = new ArrayList<Object>();
            params.add(status);
            params.add(excel_uid);
            boolean flag = this.jdbcUtils.updateByPreparedStatement(sql, params);
            return flag;
        } catch (Exception e) {
            System.out.println(e);
        }finally {
            this.jdbcUtils.releaseConnection();
        }
        return false;
    }
}
