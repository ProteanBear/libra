package xyz.proteanbear.test;

import com.alibaba.dubbo.config.annotation.Service;
import xyz.proteanbear.test.DubboHelloService;

/**
 * Dubbo welcome service.
 *
 * @author ProteanBear
 */
@Service(interfaceClass = DubboHelloService.class)
public class DubboHelloServiceImpl implements DubboHelloService
{
    /**
     * welcome.
     *
     * @return the welcome string.
     */
    @Override
    public String hello()
    {
        return "libra-dubbo";
    }
}