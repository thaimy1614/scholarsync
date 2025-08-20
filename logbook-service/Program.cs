using DotNetEnv;
using logbook_service.DbMigrator;
using logbook_service.HttpApi;
using logbook_service.Repositories.Implementations;
using logbook_service.Repositories.Interfaces;
using logbook_service.Services.Implementations;
using logbook_service.Services.Interfaces;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Any;
using Microsoft.OpenApi.Models;
using System.Security.Claims;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

//env
Env.Load();
string environment = Environment.GetEnvironmentVariable("CONNECTION_STRING");
string swagger_server = Environment.GetEnvironmentVariable("SWAGGER_SERVER");

builder.Services.AddDbContext<LogbookServiceDbContext>(options =>
    options.UseNpgsql(environment));

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddHttpClient();

//Add scope
builder.Services.AddScoped<ICompetitionDailyDetailRepository, CompetitionDailyDetailRepository>();
builder.Services.AddScoped<ICompetitionDailyRecordRepository, CompetitionDailyRecordRepository>();
builder.Services.AddScoped<ICompetitionPeriodScoreRepository, CompetitionPeriodScoreRepository>();
builder.Services.AddScoped<ICompetitionRuleRepository, CompetitionRuleRepository>();

builder.Services.AddScoped<IUnitOfWork, UnitOfWork>();

//Add services
builder.Services.AddScoped<ICompetitionDailyDetailService, CompetitionDailyDetailService>();
builder.Services.AddScoped<ICompetitionDailyRecordService, CompetitionDailyRecordService>();
builder.Services.AddScoped<ICompetitionPeriodScoreService, CompetitionPeriodScoreService>();
builder.Services.AddScoped<ICompetitionRuleService, CompetitionRuleService>();

builder.Services.AddAutoMapper(typeof(Program).Assembly);

builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo { Title = "Logbook Service", Version = "v1" });
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
            ["message"] = new OpenApiSchema { Type = "string" },
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
        Description = "Input token in format: Bearer {token}"
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
    c.RouteTemplate = "logbook/swagger/{documentName}/swagger.json";
});

app.UseSwaggerUI(c =>
{
    c.SwaggerEndpoint("/logbook/swagger/v1/swagger.json", "Logbook Service API v1");
    c.RoutePrefix = "logbook/swagger";
});

app.UseRouting();

app.UseHttpsRedirection();

app.UseAuthentication();

app.UseAuthorization();

app.MapControllers();

app.Run();
