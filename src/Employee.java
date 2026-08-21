public class Employee{

    private int employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private String status;
    private int departmentId;
    private int projectId;
    private Integer supervisorId; 

    public Employee(){}

    public Employee(int employeeId, String firstName, String lastName, String email, String password, String role, String status, int departmentId, int projectId, Integer supervisorId){
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.departmentId = departmentId;
        this.projectId = projectId;
        this.supervisorId = supervisorId;
    }

    public int getEmployeeId()       {return employeeId;}
    public String getFirstName()     {return firstName;}
    public String getLastName()      {return lastName;}
    public String getEmail()         {return email;}
    public String getPassword()      {return password;}
    public String getRole()          {return role;}
    public String getStatus()        {return status;}
    public int getDepartmentId()     {return departmentId;}
    public int getProjectId()        {return projectId;}
    public Integer getSupervisorId() {return supervisorId;}

    public void setEmployeeId(int employeeId)         {this.employeeId = employeeId;}
    public void setFirstName(String firstName)        {this.firstName = firstName;}
    public void setLastName(String lastName)          {this.lastName = lastName;}
    public void setEmail(String email)                {this.email = email;}
    public void setPassword(String password)          {this.password = password;}
    public void setRole(String role)                  {this.role = role;}
    public void setStatus(String status)              {this.status = status;}
    public void setDepartmentId(int departmentId)     {this.departmentId = departmentId;}
    public void setProjectId(int projectId)           {this.projectId = projectId;}
    public void setSupervisorId(Integer supervisorId) {this.supervisorId = supervisorId;}
}