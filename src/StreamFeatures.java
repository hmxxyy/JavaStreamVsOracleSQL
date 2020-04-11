import javafx.util.Pair;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hmxxyy
 */
public class StreamFeatures {
    static class Employee {
        Integer empNo;
        String empName;
        String job;
        Integer managerNo;
        LocalDate hireDate;
        Double salary;
        Double commission;
        Integer deptNo;
        Employee(final Integer empNo, final String empName,
                 final String job, final Integer managerNo,
                 final LocalDate hireDate, final Double salary,
                 final Double commission, Integer deptNo) {
            this.empNo = empNo; this.empName = empName;
            this.job = job; this.managerNo = managerNo;
            this.salary = salary; this.commission = commission;
            this.deptNo = deptNo; this.hireDate = hireDate;
        }
    }

    static class Department {
        Integer deptNo;
        String deptName;
        String location;
        Department(int deptNo, String deptName, String location) {
            this.deptNo = deptNo;
            this.deptName = deptName;
            this.location = location;
        }
    }

    private static final Employee [] EMPLOYEES = {
        new Employee(7839, "KING", "PRESIDENT", null,
            LocalDate.of(1981, 11, 17), 5000.0, null, 10),
        new Employee(7698, "BLAKE", "MANAGER", 7839,
            LocalDate.of(1981, 5, 1), 2850.0, null, 30),
        new Employee(7782, "CLARK", "MANAGER", 7839,
            LocalDate.of(1981, 6, 9), 2450.0, null, 10),
        new Employee(7566, "JONES", "MANAGER", 7839,
            LocalDate.of(1981, 4, 2), 2975.0, null, 20),
        new Employee(7788, "SCOTT", "ANALYST", 7566,
            LocalDate.of(1987, 7, 13).minusDays(85), 3000.0, null, 20),
        new Employee(7902, "FORD", "ANALYST", 7566,
            LocalDate.of(1981, 12, 3), 3000.0, null, 20),
        new Employee(7369, "SMITH", "CLERK", 7902,
            LocalDate.of(1980, 12, 17), 800.0, null, 20),
        new Employee(7499, "ALLEN", "SALESMAN", 7698,
            LocalDate.of(1981, 2, 20), 1600.0, 300.0, 30),
        new Employee(7521, "WARD", "SALESMAN", 7698,
            LocalDate.of(1981, 2, 22), 1250.0, 500.0, 30),
        new Employee(7654, "MARTIN", "SALESMAN", 7698,
            LocalDate.of(1981, 9, 28), 1250.0, 1400.0, 30),
        new Employee(7844, "TURNER", "SALESMAN", 7698,
            LocalDate.of(1981, 9, 8), 1500.0, 0.0, 30),
        new Employee(7876, "ADAMS", "CLERK", 7788,
            LocalDate.of(1987, 7, 13).minusDays(51), 1100.0, null, 20),
        new Employee(7900, "JAMES", "CLERK", 7698,
            LocalDate.of(1981, 12, 3), 950.0, null, 30),
        new Employee(7934, "MILLER", "CLERK", 7782,
            LocalDate.of(1982, 1, 23), 1300.0, null, 10)
    };

    private static final Department [] DEPARTMENTS = {
        new Department(10, "ACCOUNTING", "NEW YORK"),
        new Department(20, "RESEARCH", "DALLAS"),
        new Department(30, "SALES", "CHICAGO"),
        new Department(40, "OPERATIONS", "BOSTON")
    };

    public static void main(String[] args) {
        //SELECT DISTINCT JOB FROM EMPLOYEE
        Arrays.stream(EMPLOYEES)
            .map(r -> r.job)
            .distinct()
            .forEach(r -> System.out.println(r));

        //Employees without commission
        //SELECT empno FROM EMPLOYEE WHERE commission is null
        Arrays.stream(EMPLOYEES)
            .filter(r -> r.commission != null)
            .map(r -> r.empNo)
            .forEach(r -> System.out.println(r.toString()));

        //count distinct jobs
        //SELECT count(DISTINCT JOB) FROM EMPLOYEE;
        System.out.println("Number of distinct jobs: " +
            Arrays.stream(EMPLOYEES)
                .map(r -> r.job)
                .distinct()
                .count()
        );

        //lowest, highest, average, and total salary
        //SELECT min(SALARY), max(SALARY), avg(SALARY), sum(SALARY) FROM EMPLOYEE;
        DoubleSummaryStatistics result = Arrays.stream(EMPLOYEES)
            .map(r -> r.salary)
            .collect(Collectors.summarizingDouble(r -> r));
        System.out.printf("Min: %8.3f, Max: %8.3f, Avg: %8.3f, Sum: %8.3f\n",
            result.getMin(), result.getMax(), result.getAverage(), result.getSum());

        //lowest, highest, average, and total salary by department
        Arrays.stream(EMPLOYEES)
            .collect(Collectors.groupingBy(r -> r.deptNo))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(r->r.getKey(), r ->
                r.getValue()
                    .stream()
                    .map(e -> e.salary)
                    .collect(Collectors.summarizingDouble(s -> s))))
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (newValue, oldValue)-> newValue, LinkedHashMap::new)).
            forEach((k,v) ->
                System.out.printf("DeptId: %d, Min: %8.3f, Max: %8.3f, Avg: %8.3f, Sum: %8.3f\n",
                    k, v.getMin(), v.getMax(), v.getAverage(), v.getSum()));

        //Find employees with highest pay (salary + commission)
        //SELECT MAX(empno) KEEP (DENSE_RANK FIRST ORDER BY SALARY + NVL(COMMISSION, 0)
        //FROM Employee
        Arrays.stream(EMPLOYEES)
            .collect(Collectors.
                maxBy(Comparator.comparingDouble(r -> r.salary + (r.commission == null? 0.0 : r.commission))))
            .map(r -> r.empNo)
            .ifPresent(r -> System.out.println("empNo with highest pay: " + r));

        //Find employee with highest pay (salary + commission) in each department
        //SELECT deptno, MAX(empno) KEEP (DENSE_RANK FIRST ORDER BY SALARY + NVL(COMMISSION, 0) OVER (PARTITION BY deptno)
        //FROM Employee
        //GROUP BY deptno
        Arrays.stream(EMPLOYEES)
            .collect(Collectors.groupingBy(r -> r.deptNo,
                Collectors.maxBy(Comparator.comparingDouble(r -> r.salary + (r.commission == null ? 0.0 : r.commission)))))
            .values()
            .stream()
            .filter(Optional::isPresent)
            .map(r -> r.get())
            .forEach(r -> System.out.format("Employee with highest payment in Dept %d: %d\n", r.deptNo, r.empNo));

        //SELECT e.empname, d.location from employee e, department d where e.deptno = d.deptno
        Arrays.stream(EMPLOYEES).forEach(
            r -> System.out.format("Employee %s's location is: %s\n", r.empName,
                Arrays.stream(DEPARTMENTS).filter(d -> d.deptNo.equals(r.deptNo))
                    .findFirst()
                    .map(d -> d.location)
                    .orElse("")
                    )
        );
    }
}
