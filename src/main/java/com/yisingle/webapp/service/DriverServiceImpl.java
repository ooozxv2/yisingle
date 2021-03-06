package com.yisingle.webapp.service;

import com.yisingle.webapp.dao.DriverDao;
import com.yisingle.webapp.dao.OrderDao;
import com.yisingle.webapp.data.*;
import com.yisingle.webapp.entity.DriverEntity;
import com.yisingle.webapp.entity.OrderEntity;
import com.yisingle.webapp.websocket.SystemWebSocketHandler;
import org.apache.commons.logging.impl.SimpleLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jikun on 17/6/26.
 */
@Service("driverService")
@Transactional
public class DriverServiceImpl implements DriverService {

    private SimpleLog simpleLog = new SimpleLog(DriverServiceImpl.class.getSimpleName());

    @Autowired
    private DriverDao driverDao;

    @Autowired
    private OrderDao orderDao;

    public ResponseData saveDriver(DriverRegisterRequestData driverRequestData) {
        ResponseData data = new ResponseData();
        List<DriverEntity> driverEntityList = driverDao.findDriverByPhoneNum(driverRequestData.getPhonenum());

        if (null != driverEntityList && driverEntityList.size() > 0) {
            data.setCode(ResponseData.Code.FAILED.value());
            data.setErrorMsg("司机手机号已存在");
        } else {
            DriverEntity driver = new DriverEntity();
            driver.setDriverName(driverRequestData.getDriverName());
            driver.setPhonenum(driverRequestData.getPhonenum());
            driver.setPassword(driverRequestData.getPassword());
            driver.setLatitude("30.538196");//默认都在软件园E区
            driver.setLongitude("104.068359");//默认都在软件园E区
            driverDao.save(driver);

            data.setCode(ResponseData.Code.SUCCESS.value());
            data.setResponse(driver);

        }

        return data;
    }

    public ResponseData<DriverEntity> loginDriver(DriverLoginRequestData loginRequestData) {
        ResponseData data = new ResponseData();

        List<DriverEntity> driverEntityList = driverDao.findDriverByPhoneNum(loginRequestData.getPhonenum());

        if (null != driverEntityList && driverEntityList.size() > 0) {

            DriverEntity entity = driverEntityList.get(0);

            if (entity.getPassword().equals(loginRequestData.getPassword())) {
                data.setCode(ResponseData.Code.SUCCESS.value());

                data.setResponse(entity);
            } else {

                data.setCode(ResponseData.Code.FAILED.value());
                data.setErrorMsg("密码错误");
            }


        } else {

            data.setCode(ResponseData.Code.FAILED.value());
            data.setErrorMsg("司机的账号不存在");

        }
        return data;
    }

    public ResponseData<DriverEntity> changeDriverState(DriverStateRequestData stateRequestData) {


        ResponseData data = new ResponseData();

        List<DriverEntity> driverEntityList = driverDao.findDriverByPhoneNum(stateRequestData.getPhonenum());

        if (null != driverEntityList && driverEntityList.size() > 0) {

            DriverEntity entity = driverEntityList.get(0);

            entity.setState(stateRequestData.getState());

            driverDao.save(entity);

            data.setCode(ResponseData.Code.SUCCESS.value());
            data.setResponse(entity);


        } else {

            data.setCode(ResponseData.Code.FAILED.value());
            data.setErrorMsg("司机的账号不存在");

        }
        return data;
    }

    /**
     * 保存坐标点到司机的数据表中
     */
    public void saveLocationPointToDriver(HeartBeatData data) {
        if (null != data & !data.getLatitude().equals("") && !data.getLongitude().equals("")) {
            DriverEntity driverEntity = driverDao.findById(data.getId());
            if (null != driverEntity) {
                driverEntity.setLatitude(data.getLatitude());
                driverEntity.setLongitude(data.getLongitude());
                driverEntity.setOnlineTime(System.currentTimeMillis());
                driverDao.save(driverEntity);
            } else {
                simpleLog.info("未能查询当前司机不能保存坐标到数据库 ");
            }
        }


    }


}
