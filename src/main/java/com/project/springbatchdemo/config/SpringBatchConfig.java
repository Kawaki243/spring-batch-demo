package com.project.springbatchdemo.config;


import com.project.springbatchdemo.entity.UserEntity;
import com.project.springbatchdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.project.springbatchdemo.config.UserProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Lớp cấu hình Spring Batch định nghĩa Job, Step, Reader, Processor và Writer
 * để thực hiện import data {@link UserEntity} từ file CSV vào cơ sở data.
 * <p>
 * Cấu hình này thiết lập một Job có tên {@code importUsers} với luồng xử lý:
 * <ul>
 *     <li>Đọc data từ file CSV {@code users-1000.csv}</li>
 *     <li>Map từng dòng thành đối tượng {@link UserEntity}</li>
 *     <li>Xử lý data (chuyển tên sang chữ hoa) bằng {@link UserProcessor}</li>
 *     <li>Lưu data vào database qua {@link UserRepository}</li>
 * </ul>
 * </p>
 */
@Configuration // Đánh dấu đây là một lớp cấu hình Spring
public class SpringBatchConfig {

    @Autowired // Tự động inject UserRepository do Spring quản lý
    private UserRepository userRepository;

    /**
     * Khai báo {@link FlatFileItemReader} để đọc data người dùng từ file CSV.
     */
    @Bean // Đăng ký một bean reader trong Spring Context
    public FlatFileItemReader<UserEntity> reader() {
        return new FlatFileItemReaderBuilder<UserEntity>() // Sử dụng builder để tạo Reader
                .name("userItemReader") // Đặt tên cho reader
                .resource(new ClassPathResource("users-1000.csv")) // Chỉ định file CSV nguồn (trong resources)
                .linesToSkip(1) // Bỏ qua dòng đầu tiên (header)
                .lineMapper(lineMapper()) // Gắn lineMapper để map data
                .targetType(UserEntity.class) // Đối tượng đích là UserEntity
                .build(); // Xây dựng reader hoàn chỉnh
    }

    /**
     * Cấu hình {@link LineMapper} để ánh xạ từng dòng CSV thành {@link UserEntity}.
     */
    private LineMapper<UserEntity> lineMapper() {
        DefaultLineMapper<UserEntity> lineMapper = new DefaultLineMapper<>(); // Tạo LineMapper mặc định

        // Tách các field data trong mỗi dòng CSV
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(","); // Tách bằng dấu phẩy
        lineTokenizer.setStrict(false);  // linh hoạt hơn, giúp batch job không bị fail khi CSV thiếu hoặc thừa cột.
        lineTokenizer.setNames("id","userId", "firstName", "lastName", "gender",
                "email", "phone", "dateOfBirth", "jobTitle"); // Đặt tên cột tương ứng với UserEntity

        // BeanWrapperFieldSetMapper sẽ :
        // Map các field đã parse sang entity
        // Tìm trong UserEntity các field trùng tên (userId, firstName, …).
        // Gán giá trị tương ứng.
        // Trả về một UserEntity đã được populate.
        BeanWrapperFieldSetMapper<UserEntity> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(UserEntity.class); // Chỉ định rằng tất cả data CSV sau khi parse sẽ được ánh xạ vào UserEntity.

        // Gắn tokenizer và mapper vào lineMapper
        lineMapper.setLineTokenizer(lineTokenizer); // chịu trách nhiệm cắt (tokenize) một dòng text (String) thành các cột data (Field).
        lineMapper.setFieldSetMapper(fieldSetMapper); // Sau khi tokenizer cắt dòng CSV thành các giá trị, FieldSetMapper sẽ map các giá trị đó thành một object ( UserEntity ).

        return lineMapper; // Trả về lineMapper đã cấu hình
    }

    /**
     * Khai báo bean {@link UserProcessor} để xử lý data {@link UserEntity}.
     * <p>
     *    Nhiệm vụ của {@link UserProcessor} là chuyển đổi các field FirstName và LastName thành UpperCase()
     * </p>
     */
    @Bean
    UserProcessor userProcessor() {
        return new UserProcessor(); // Trả về một instance UserProcessor
    }

    /**
     * Cấu hình {@link RepositoryItemWriter} để ghi data ( sau khi được Reader đọc và Processor xử lý ) {@link UserEntity}
     * vào database thông qua {@link UserRepository}.
     */
    @Bean
    RepositoryItemWriter<UserEntity> writer() {
        RepositoryItemWriter<UserEntity> writer = new RepositoryItemWriter<>(); // Tạo writer mới
        writer.setRepository(this.userRepository); // Repository sẽ được gọi để lưu data
        writer.setMethodName("save"); // Ở đây ta chỉ định "save", tức là writer sẽ gọi userRepository.save(userEntity) cho từng data.
        return writer; // Trả về writer đã cấu hình
    }

    /**
     * Khai báo batch {@link Job} với tên {@code importUsers}.
     * <p>
     *     Tạo một Job mới tên là importUsers, quản lý bởi jobRepository, bắt đầu chạy với step csv-import-step, và trả về một Job hoàn chỉnh.
     * </p>
     */
    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("importUsers", jobRepository) // Tạo JobBuilder với tên importUsers. Mỗi lần job chạy, thông tin này sẽ được lưu trong bảng metadata của Spring Batch (ví dụ: BATCH_JOB_INSTANCE, BATCH_JOB_EXECUTION).
                .start(step) // Job bắt đầu bằng step này, Chỉ định rằng Job này sẽ bắt đầu thực thi bằng step được truyền vào. Ở đây step chính là csv-import-step ta đã định nghĩa bên dưới.
                .build(); // Xây dựng Job hoàn chỉnh
    }

    /**
     * Khai báo batch {@link Step} để đọc, xử lý và ghi data người dùng.
     * <p>
     *     Đây là một Step trong Spring Batch, tên là csv-import-step.
     *     Step này chính là logic “CSV → Reader → Processor → Writer → Database”, được chạy theo từng mỗi 10 transactions.
     * </p>
     * <p>
     *     Step này được định nghĩa theo chunk-oriented processing, tức là :
     * </p>
     * <ul>
     *     <li>
     *         Đọc dữ liệu từ CSV (ItemReader).
     *     </li>
     *     <li>
     *          Xử lý dữ liệu (ItemProcessor).
     *      </li>
     *      <li>
     *          Ghi dữ liệu ra DB (ItemWriter).
     *      </li>
     *      <li>
     *          Sau khi xử lý xong 10 bản ghi (chunk size = 10) thì mới commit transaction 1 lần.
     *      </li>
     * </ul>
     *
     */
    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("csv-import-step", jobRepository) // Đây là một Step trong Spring Batch, tên là csv-import-step.
                .<UserEntity, UserEntity>chunk(10, transactionManager) // Xử lý theo chunk 10 bản ghi, commit sau mỗi 10 bản ghi
                .reader(reader()) // Đọc dữ liệu từ CSV ( "ItemReader" : FlatFileItemReader<UserEntity> reader() ).
                .processor(userProcessor()) // Xử lý dữ liệu ( "ItemProcessor" : UserProcessor userProcessor() ).
                .writer(writer()) // Ghi data vào database ( "ItemWriter" : RepositoryItemWriter<UserEntity> ).
                .build(); // Xây dựng step hoàn chỉnh
    }
}


