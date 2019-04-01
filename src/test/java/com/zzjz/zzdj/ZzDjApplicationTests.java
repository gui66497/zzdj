package com.zzjz.zzdj;

import com.zzjz.zzdj.util.AutoRunner;
import com.zzjz.zzdj.bean.Business;
import com.zzjz.zzdj.service.BusinessService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.FileNotFoundException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ZzDjApplicationTests {

    @Autowired
    BusinessService businessService;

    @Test
    public void contextLoads() {
    }

    @Test
    public void test1() throws FileNotFoundException {
        //JsonObject object = ntopUtil.getA();
        //System.out.println(object);
        System.out.println(AutoRunner.ipMacMap);
    }

    @Test
    public void testGetAllBusiness() throws FileNotFoundException {
        List<Business> businesses = businessService.getAllBusiness();
        System.out.println(businesses);
    }

}
