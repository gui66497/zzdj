package com.zzjz.zzdj;

import com.zzjz.zzdj.util.AutoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.FileNotFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ZzDjApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void test1() throws FileNotFoundException {
        //JsonObject object = ntopUtil.getA();
        //System.out.println(object);
        System.out.println(AutoRunner.ipMacMap);
    }
}
