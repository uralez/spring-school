package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/students")
public class AdminStudentController {

    private final StudentService studentService;

    @GetMapping
    public String groups(Model model) {
        model.addAttribute("groups", studentService.groups());
        return "admin/students";
    }

    @GetMapping("/{groupId}")
    public String group(@PathVariable Long groupId, Model model) {
        model.addAttribute("group", studentService.group(groupId));
        model.addAttribute("members", studentService.members(groupId));
        return "admin/students-group";
    }

    @PostMapping("/{groupId}")
    public String add(@PathVariable Long groupId, @RequestParam String fullName) {
        if (!fullName.isBlank()) {
            studentService.addStudent(groupId, fullName);
        }
        return "redirect:/admin/students/" + groupId;
    }
}