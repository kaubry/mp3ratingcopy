package watershine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class Main extends Application {

//    @Autowired
//    private ITunesXMLParser iTunesXMLParser;
//
//    @Autowired
//    private RatingCopyProcessor ratingCopyProcessor;

//    @Autowired
//    private MainFrame mainFrame;

    private ConfigurableApplicationContext springContext;
    private Parent rootNode;


    public static void main(String[] args) {
        Application.launch(args);
//        SpringApplication.run(Application.class, args);
    }

    @Override
    public void init() throws Exception {
//        ConfigurableApplicationContext context = new SpringApplicationBuilder(Main.class).headless(false).run(args);
        springContext = SpringApplication.run(Main.class);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);
        rootNode = fxmlLoader.load();

//        showMainWindow();

//        ArrayList<Song> songs = iTunesXMLParser.getLibrary();
//        ratingCopyProcessor.copyRatingsIntoComposer(songs);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(rootNode);
        stage.setScene(scene);
        stage.setTitle("Mp3 Ratings Copy Tool");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        springContext.close();
    }

//    private void showMainWindow() {
//        mainFrame.addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent windowEvent){
//                System.exit(0);
//            }
//        });
//        mainFrame.setVisible(true);
//        SwingUtilities.updateComponentTreeUI(mainFrame);
//    }


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
