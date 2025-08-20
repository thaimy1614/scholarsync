using AutoMapper;
using DotNetEnv;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Any;
using Microsoft.OpenApi.Models;
using subject_service.Common.Exporting.Implementations;
using subject_service.Common.Exporting.Interfaces;
using subject_service.Common.Results;
using subject_service.DbMigrator;
using subject_service.Repositories.Implementations;
using subject_service.Repositories.Interfaces;
using subject_service.SecurityJWT;
using subject_service.Services.Implementations;
using subject_service.Services.Interfaces;
using System.Security.Claims;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

//env
Env.Load();
string environment = Environment.GetEnvironmentVariable("CONNECTION_STRING");
string swagger_server = Environment.GetEnvironmentVariable("SWAGGER_SERVER");

// Register services
builder.Services.AddDbContext<SubjectServiceDbContext>(options =>
    options.UseNpgsql(environment));
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddHttpClient();

//Add repo
builder.Services.AddScoped<ISubjectRepository, SubjectRepository>();
builder.Services.AddScoped<IMarkRepository, MarkRepository>();
builder.Services.AddScoped<IMarkSubjectRepository, MarkSubjectRepository>();
builder.Services.AddScoped<IComponentPointRepository, ComponentPointRepository>();
builder.Services.AddScoped<IMarkTypeRepository, MarkTypeRepository>();
builder.Services.AddScoped<ITeacherSubjectClassRepository, TeacherSubjectClassRepository>();
builder.Services.AddScoped<ISubjectClassRepository, SubjectClassRepository>();
builder.Services.AddScoped<IViewStudentSubjectScoreRepository, ViewStudentSubjectScoreRepository>();
builder.Services.AddScoped<IUnitOfWork, UnitOfWork>();

//Add service
builder.Services.AddScoped<ISubjectService, SubjectService>();
builder.Services.AddScoped<IMarkService, MarkService>();
builder.Services.AddScoped<IMarkSubjectService, MarkSubjectService>();
builder.Services.AddScoped<IComponentPointService, ComponentPointService>();
builder.Services.AddScoped<IMarkTypeService, MarkTypeService>();
builder.Services.AddScoped<ITeacherSubjectClassService, TeacherSubjectClassService>();
builder.Services.AddScoped<ISubjectClassService, SubjectClassService>();
builder.Services.AddScoped<IUserClientService, UserClientService>();
builder.Services.AddScoped<ISchoolClientService, SchoolClientService>();


//Add config jwt
builder.Services.AddScoped<IClaimsTransformation, ScopeToRolesTransformer>();

builder.Services.AddScoped<IExcelExporter, ExcelExporter>();
builder.Services.AddScoped<IMarkExporter, MarkExporter>();

builder.Services.AddAutoMapper(typeof(Program).Assembly);

builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo { Title = "Subject Service", Version = "v1" });
    options.AddServer(new OpenApiServer
        {
            Url = swagger_server,
            Description = "API Gateway"
        });
    options.MapType<ApiResponse<object>>(() => new OpenApiSchema
    {
        Properties = new Dictionary<string, OpenApiSchema>
        {
            ["code"] = new OpenApiSchema { Type = "int", Example = new OpenApiInteger(1000) },
            ["message"] = new OpenApiSchema { Type = "string"},
            ["result"] = new OpenApiSchema { Type = "object" }
        }
    });

    options.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Name = "Authorization",
        Type = SecuritySchemeType.Http,
        Scheme = "Bearer",
        BearerFormat = "JWT",
        In = ParameterLocation.Header,
        Description = "Enter token in format: Bearer {token}"
    });

    options.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            new List<string>()
        }
    });
});

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.RequireHttpsMetadata = false;
        options.SaveToken = true;
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = false,
            ValidateAudience = false,
            ValidateLifetime = false,
            ValidateIssuerSigningKey = false,
            ValidIssuer = builder.Configuration["Jwt:Issuer"],
            ValidAudience = builder.Configuration["Jwt:Audience"],
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(builder.Configuration["Jwt:Key"])),
            ValidAlgorithms = new[] { SecurityAlgorithms.HmacSha512 },
            RoleClaimType = ClaimTypes.Role
        };
        options.MapInboundClaims = false;
    });

builder.Services.AddAuthorization();

var app = builder.Build();
    
app.UseSwagger(c =>
{
    c.RouteTemplate = "subject/swagger/{documentName}/swagger.json";
});

app.UseSwaggerUI(c =>
{
    c.SwaggerEndpoint("/subject/swagger/v1/swagger.json", "Subject Service API v1");
    c.RoutePrefix = "subject/swagger";
});

app.UseRouting();

app.UseHttpsRedirection();

app.UseAuthentication();

app.UseAuthorization();

app.MapControllers();

app.Run();
