package com.project.springbatchdemo.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobLauncher jobLauncher; // JobLauncher : Component của Spring Batch, dùng để chạy một Job với các @Param ( JobParameters ).

    @Autowired
    private Job job; // Inject (@Autowired) Job đã được ta định nghĩa trong SpringBatchConfig ( tên job: importUsers trong public job(JobRepository jobRepository, Step step)).

    /**
     * Tạo API POST /jobs/importData để khi gọi sẽ chạy job.
     * Trả về String biểu diễn trạng thái job (COMPLETED, FAILED, …).
     * */
    @PostMapping("/importData")
    public String jobLauncher() {
        // Mỗi lần chạy job, bạn phải truyền vào JobParameters.
        // Ở đây thêm một tham số startAt = System.currentTimeMillis() để đảm bảo JobInstance luôn unique.
        // ( Nếu không, Spring Batch sẽ nghĩ job đã chạy rồi và không cho chạy lại ).
        final JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();
        try {
            // jobLauncher.run(...) sẽ :
            // 1. Lấy Job bạn đã cấu hình (ở đây là importUsers).
            // 2. Nhận jobParameters (tham số chạy job, đảm bảo job instance là duy nhất).
            // 3. Gửi job này cho Spring Batch JobLauncher để bắt đầu chạy.
            final JobExecution jobExecution = this.jobLauncher.run(this.job, jobParameters);
            // JobExecution
            // Kết quả của việc chạy job, chứa thông tin :
            // 1. getStatus() → trạng thái (STARTED, COMPLETED, FAILED, …).
            // 2. getStartTime() → thời gian bắt đầu.
            // 3. getEndTime() → thời gian kết thúc.
            // 4. getExitStatus() → lý do kết thúc job.
            return jobExecution.getStatus().toString();
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            // Đây là cách in stack trace (chuỗi thông tin về lỗi) của exception ra console (System.err).
            // Khi xảy ra lỗi, printStackTrace() sẽ hiển thị :
            //
            //  1. Loại exception.
            //  2. Thông điệp lỗi.
            //  3. Vị trí trong code (file, dòng).
            //  4. Toàn bộ chuỗi gọi hàm dẫn tới lỗi (stack trace).
            e.printStackTrace();
            return "Job failed with exception: " + e.getMessage();
        }
    }
}
