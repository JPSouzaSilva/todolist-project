package br.com.joaosilva.todolist.task;

import br.com.joaosilva.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity<?> create(@RequestBody Task task, HttpServletRequest request) {
        var id_user = request.getAttribute("idUser");
        task.setIdUser((UUID) id_user);
        var current_date = LocalDateTime.now();
        if (current_date.isAfter(task.getStartAt()) || current_date.isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de inicio deve ser maior que a data atual");
        }
        if (task.getEndAt().isBefore(task.getStartAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data final deve ser maior que a data inicial");
        }
        var create_task = this.taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(create_task);
    }

    @GetMapping("/")
    public List<Task> listTasks(HttpServletRequest request) {
        var id_user = request.getAttribute("idUser");
        return this.taskRepository.findByIdUser((UUID) id_user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody Task taskModel, HttpServletRequest request, @PathVariable UUID id) {
        var task = this.taskRepository.findById(id).orElse(null);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada");
        }
        var id_user = request.getAttribute("idUser");
        if (!task.getIdUser().equals(id_user)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voce não tem permissão para alterar essa tarefa");
        }
        Utils.copyNonNullProperties(taskModel, task);
        var task_update = this.taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.OK).body(task_update);
    }
}
