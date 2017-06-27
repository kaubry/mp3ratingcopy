package watershine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import watershine.gui.MainFrame;
import watershine.itunes.ITunesXMLParser;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private ITunesXMLParser iTunesXMLParser;

    @Autowired
    private RatingCopyProcessor ratingCopyProcessor;

    @Autowired
    private MainFrame mainFrame;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(Application.class).headless(false).run(args);

//        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        showMainWindow();
//        ArrayList<Song> songs = iTunesXMLParser.getLibrary();
//        ratingCopyProcessor.copyRatingsIntoComposer(songs);
    }

    private void showMainWindow() {
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        mainFrame.setVisible(true);
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
