package watershine;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import watershine.itunes.jax2b.Dict;
import watershine.itunes.jax2b.DictArray;
import watershine.itunes.jax2b.SongLibrary;

@Configuration
public class AppConfig {

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(new Class[]{
                SongLibrary.class,
                Dict.class,
                DictArray.class
        });
        marshaller.setSupportDtd(true);
        return marshaller;
    }

    @Bean
    public SimpleAsyncTaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

}
