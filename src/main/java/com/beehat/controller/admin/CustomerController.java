package com.beehat.controller.admin;

import com.beehat.entity.Customer;
import com.beehat.repository.CustomerRepo;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/customer")
public class CustomerController {
    @Autowired
    CustomerRepo customerRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;



    @GetMapping
    public String customers(
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        model.addAttribute("customer",new Customer() );
        Pageable pageable = PageRequest.of(page, size);
        List<Customer> customers = customerRepo.findAll(pageable).getContent();

        model.addAttribute("customers", customers);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", customerRepo.findAll(pageable).getTotalPages());

        return "admin/customer";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id){
        customerRepo.deleteById(id);
        return "redirect:/admin/customer";
    }

    @PostMapping("/add")
    public ModelAndView add(@ModelAttribute("customer") Customer customer, BindingResult bindingResult){
        ModelAndView modelAndView  = new ModelAndView("admin/customer"); // Trả về trang admin/employee nếu có lỗi

        // Kiểm tra lỗi và thêm thông báo lỗi vào bindingResult
        if (customerRepo.existsByUsername(customer.getUsername())) {
            bindingResult.rejectValue("username", "error.customer", "Username đã tồn tại");
        }
        if (customerRepo.existsByEmail(customer.getEmail())) {
            bindingResult.rejectValue("email", "error.customer", "Email đã tồn tại");
        }
        if (customerRepo.existsByPhone(customer.getPhone())) {
            bindingResult.rejectValue("phone", "error.employee", "Số điện thoại đã tồn tại");
        }

        // Kiểm tra nếu có lỗi, trả về trang với modal mở kèm thông báo lỗi
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("customer", customer);
            modelAndView.addObject("showModal", true); // Thêm flag để JavaScript nhận biết cần hiển thị modal
            return modelAndView;
        }
        customer.setCreatedDate(LocalDateTime.now());
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customerRepo.save(customer);
        return new ModelAndView("redirect:/admin/customer") ;
    }

    @GetMapping("/detail/{id}")
    public String editcustomer(@PathVariable("id") Integer id, Model model) {
        Customer customer = customerRepo.findeById(id);
        if (customer!=null) {
            model.addAttribute("customer", customer);
            return "/customer/customerDetail";
        }
        return "redirect:/admin/customer";
    }

    @PostMapping("/update")
    public String updateCustomer(@ModelAttribute Customer customer) {
        customer.setUpdatedDate(LocalDateTime.now());
//        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customerRepo.save(customer);
        return "redirect:/admin/customer";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "searchValue", required = false) String searchValue,
                         @RequestParam(value = "status", required = false) String status,
                         Model model){
        List<Customer> customers;
        Byte statusValue = null;
        model.addAttribute("customer", new Customer());
        // Chuyển đổi status nếu không null hoặc rỗng
        if (status != null && !status.isEmpty()) {
            try {
                statusValue = Byte.parseByte(status);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

// Nếu searchValue không được điền, tìm tất cả nhân viên
        if (searchValue == null || searchValue.isEmpty()) {
            customers = customerRepo.findAllCustomers(statusValue);
        } else {
            // Nếu có searchValue, tìm theo tên, username hoặc SĐT
            customers = customerRepo.findCustomersBySearchValue(searchValue, statusValue);
        }

        // Gán danh sách nhân viên vào model
        model.addAttribute("customers", customers);
        return "/admin/customer";
    }

    @GetMapping("/export")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response) throws IOException {
        String fileName = "customers.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Customers");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Username");
        headerRow.createCell(1).setCellValue("Password");
        headerRow.createCell(2).setCellValue("Fullname");
        headerRow.createCell(3).setCellValue("Email");
        headerRow.createCell(4).setCellValue("Address");
        headerRow.createCell(5).setCellValue("City");
        headerRow.createCell(6).setCellValue("District");
        headerRow.createCell(7).setCellValue("Ward");
        headerRow.createCell(8).setCellValue("Country");
        headerRow.createCell(9).setCellValue("Photo");
        headerRow.createCell(10).setCellValue("Phone");
        headerRow.createCell(11).setCellValue("Status");
        headerRow.createCell(12).setCellValue("Created Date");

        List<Customer> customers = customerRepo.findAll();

        int rowNum = 1;
        for (Customer customer : customers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(customer.getUsername());
            row.createCell(1).setCellValue(customer.getPassword());
            row.createCell(2).setCellValue(customer.getFullname());
            row.createCell(3).setCellValue(customer.getEmail());
            row.createCell(4).setCellValue(customer.getAddress());
            row.createCell(5).setCellValue(customer.getCity());
            row.createCell(6).setCellValue(customer.getDistrict());
            row.createCell(7).setCellValue(customer.getWard());
            row.createCell(8).setCellValue(customer.getCountry());
            row.createCell(9).setCellValue(customer.getPhoto());
            row.createCell(10).setCellValue(customer.getPhone());
            row.createCell(11).setCellValue(customer.getStatus());
            row.createCell(12).setCellValue(customer.getCreatedDate() != null ? customer.getCreatedDate().toString() : null);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @PostMapping("/import")
    public String importCustomers(@RequestParam("file") MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())){
            Sheet sheet = workbook.getSheetAt(0);
            List<Customer> customers = new ArrayList<>();
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String username = row.getCell(1).getStringCellValue();
                    String password = row.getCell(2).getStringCellValue();
                    String fullname = row.getCell(3).getStringCellValue();
                    String email = row.getCell(4).getStringCellValue();
                    String address = row.getCell(5).getStringCellValue();
                    String city = row.getCell(6).getStringCellValue();
                    String district = row.getCell(7).getStringCellValue();
                    String ward = row.getCell(8).getStringCellValue();
                    String country = row.getCell(9).getStringCellValue();
                    String photo = row.getCell(10).getStringCellValue();
                    String phone = row.getCell(11).getStringCellValue();
                    int statusValue = (int) row.getCell(12).getNumericCellValue();
                    byte status = (byte) (statusValue >= Byte.MIN_VALUE && statusValue <= Byte.MAX_VALUE ? statusValue : 0);
                    // khởi tạo hàm
                    Customer customer = new Customer(username, password, fullname, email, address, city, district, ward, country, photo, phone, status);
                    customer.setCreatedDate(LocalDateTime.now());
                    customers.add(customer);
                }
            }
            customerRepo.saveAll(customers);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/admin/customer";
    }
}
