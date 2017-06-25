package watershine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import watershine.itunes.ITunesXMLParser;
import watershine.model.Song;

import java.util.ArrayList;


@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private ITunesXMLParser iTunesXMLParser;

    @Autowired
    private RatingCopyProcessor ratingCopyProcessor;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        ArrayList<Song> songs = iTunesXMLParser.getSongs();
        ratingCopyProcessor.copyRatingsIntoComposer(songs);
    }


//    @Bean
//    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
//        return args -> {
//
//            System.out.println("Let's inspect the beans provided by Spring Boot:");
//
//            String[] beanNames = ctx.getBeanDefinitionNames();
//            Arrays.sort(beanNames);
//            for (String beanName : beanNames) {
//                System.out.println(beanName);
//            }
//
//        };
//    }
}
