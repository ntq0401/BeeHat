package com.beehat.controller.admin;

import com.beehat.entity.Employee;
import com.beehat.repository.EmployeeRepo;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/employee")
public class EmployeeController {
    @Autowired
    EmployeeRepo employeeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @GetMapping
    public String employee(
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        List<Employee> employees = employeeRepository.findAll(pageable).getContent();

        model.addAttribute("employees", employees);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", employeeRepository.findAll(pageable).getTotalPages());

        return "admin/employee";
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id){
        employeeRepository.deleteById(id);
        return "redirect:/admin/employee";
    }
    @GetMapping("/detail/{id}")
    public String editEmployee(@PathVariable("id") Integer id, Model model) {
        Employee employee = employeeRepository.findeById(id);
        if (employee!=null) {
            model.addAttribute("employee", employee);
            return "/admin/employee_detail";
        }
        return "redirect:/admin/employee";
    }

    @PostMapping("/update")
    public String updateEmployee(@ModelAttribute Employee employee) {
        employee.setUpdatedDate(LocalDateTime.now());
        employeeRepository.save(employee);
        return "redirect:/admin/employee";
    }
    @PostMapping("/add")
    public String add(@ModelAttribute("employee") Employee employee){
        employee.setCreatedDate(LocalDateTime.now());
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        employeeRepository.save(employee);
        return "redirect:/admin/employee";
    }
    @GetMapping("/search")
    public String search(@RequestParam("searchValue") String searchValue,
                         Model model){
        if (searchValue == null || searchValue.trim().isEmpty()) {
            model.addAttribute("employees", employeeRepository.findAll());
            return "/admin/employee";
        }
//        List<Employee> list = employeeRepository.findAll().stream()
//                .filter(e -> e.getFullname().contains(searchValue) || e.getUsername().contains(searchValue))
//                .collect(Collectors.toList());

        List<Employee> list = new ArrayList<>();
        for (Employee e:employeeRepository.findAll()){
            if (e.getUsername().contains(searchValue)|| e.getFullname().contains(searchValue)){
                list.add(e);
            }
        }
        model.addAttribute("employees", list);
        return "/admin/employee";
    }
    @GetMapping("/export")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response) throws IOException {
        // Đặt tên cho file Excel
        String fileName = "employees.xlsx";

        // Đặt kiểu nội dung cho phản hồi HTTP là Excel
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // Khởi tạo workbook và sheet trong file Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");

        // Tạo dòng đầu tiên cho tiêu đề của bảng
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Username");
        headerRow.createCell(1).setCellValue("Password");
        headerRow.createCell(2).setCellValue("Fullname");
        headerRow.createCell(3).setCellValue("Email");
        headerRow.createCell(4).setCellValue("Phone");
        headerRow.createCell(5).setCellValue("Created Date");
        headerRow.createCell(6).setCellValue("Updated Date");
        headerRow.createCell(7).setCellValue("Role");
        headerRow.createCell(8).setCellValue("Status");

        // Lấy danh sách nhân viên từ cơ sở dữ liệu
        List<Employee> employees = employeeRepository.findAll();

        // Duyệt qua danh sách nhân viên và tạo các dòng cho từng nhân viên
        int rowNum = 1; // Bắt đầu từ dòng thứ hai vì dòng đầu tiên đã là tiêu đề
        for (Employee employee : employees) {
            Row row = sheet.createRow(rowNum++);

            // Điền dữ liệu của từng nhân viên vào các cột tương ứng
            row.createCell(0).setCellValue(employee.getUsername());
            row.createCell(1).setCellValue(employee.getPassword()); // Có thể cần mã hóa hoặc ẩn dữ liệu mật khẩu
            row.createCell(2).setCellValue(employee.getFullname());
            row.createCell(3).setCellValue(employee.getEmail());
            row.createCell(4).setCellValue(employee.getPhone());
            row.createCell(5).setCellValue(employee.getCreatedDate().toString());
            row.createCell(6).setCellValue(employee.getUpdatedDate() != null ? employee.getUpdatedDate().toString() : null);
            row.createCell(7).setCellValue(employee.getRole());
            row.createCell(8).setCellValue(employee.getStatus());
        }

        // Ghi workbook ra output stream của phản hồi HTTP
        workbook.write(response.getOutputStream());

        // Đóng workbook để giải phóng tài nguyên
        workbook.close();
    }
    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        // Kiểm tra nếu file trống thì không thực hiện gì cả
        if (file.isEmpty()) {
            // Bạn có thể thêm xử lý để báo lỗi cho người dùng ở đây
            return "redirect:/admin/employee?error=file_empty";
        }

        List<Employee> list = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            // Lấy sheet đầu tiên trong workbook
            Sheet sheet = workbook.getSheetAt(0);

            // Duyệt qua từng hàng, bắt đầu từ hàng thứ 2 (index 1) để bỏ qua tiêu đề
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    // Đọc từng ô dữ liệu trong hàng và gán vào các thuộc tính của Employee
                    String username = row.getCell(0).getStringCellValue();
                    String password = row.getCell(1).getStringCellValue();
                    String fullname = row.getCell(2).getStringCellValue();
                    String email = row.getCell(3).getStringCellValue();
                    String phone = row.getCell(4).getStringCellValue();
                    Byte role = (byte) ((int) row.getCell(5).getNumericCellValue());
                    Byte status = (byte) ((int) row.getCell(6).getNumericCellValue());
                    String photo = row.getCell(7).getStringCellValue();

                    // Tạo đối tượng Employee mới và gán giá trị
                    Employee employee = new Employee(username, password, fullname, email, phone, photo, role, status);
                    employee.setCreatedDate(LocalDateTime.now());

                    // Mã hóa mật khẩu trước khi lưu
                    employee.setPassword(passwordEncoder.encode(employee.getPassword()));

                    // Thêm employee vào danh sách để lưu
                    list.add(employee);
                }
            }

            // Lưu tất cả các nhân viên đã tạo vào cơ sở dữ liệu
            employeeRepository.saveAll(list);

        } catch (IOException e) {
            e.printStackTrace();
            // Xử lý ngoại lệ có thể trả về thông báo lỗi hoặc log chi tiết hơn
            return "redirect:/admin/employee?error=import_failed";
        }

        // Chuyển hướng về trang danh sách nhân viên sau khi hoàn thành import
        return "redirect:/admin/employee";
    }
}
