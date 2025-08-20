using Microsoft.EntityFrameworkCore.Migrations;
using Npgsql.EntityFrameworkCore.PostgreSQL.Metadata;

#nullable disable

namespace subject_service.Migrations
{
    /// <inheritdoc />
    public partial class initV1 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "marks",
                columns: table => new
                {
                    id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    score_average = table.Column<decimal>(type: "numeric", nullable: true),
                    weight = table.Column<decimal>(type: "numeric", nullable: false),
                    academic_performance = table.Column<string>(type: "character varying(20)", maxLength: 20, nullable: true),
                    conduct = table.Column<string>(type: "character varying(20)", maxLength: 20, nullable: true),
                    student_id = table.Column<string>(type: "character varying(255)", maxLength: 255, nullable: false),
                    class_id = table.Column<long>(type: "bigint", nullable: false),
                    semester_id = table.Column<long>(type: "bigint", nullable: false),
                    school_year_id = table.Column<long>(type: "bigint", nullable: false),
                    is_deleted = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_marks", x => x.id);
                });

            migrationBuilder.CreateTable(
                name: "subjects",
                columns: table => new
                {
                    id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    name = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    is_scoreable = table.Column<bool>(type: "boolean", nullable: false),
                    is_evaluate_with_score = table.Column<bool>(type: "boolean", nullable: false),
                    school_year_id = table.Column<long>(type: "bigint", nullable: false),
                    is_deleted = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_subjects", x => x.id);
                });

            migrationBuilder.CreateTable(
                name: "mark_subject",
                columns: table => new
                {
                    id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    score_average = table.Column<decimal>(type: "numeric", nullable: true),
                    mark_id = table.Column<long>(type: "bigint", nullable: false),
                    subject_id = table.Column<long>(type: "bigint", nullable: false),
                    is_deleted = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_mark_subject", x => x.id);
                    table.ForeignKey(
                        name: "FK_mark_subject_marks_mark_id",
                        column: x => x.mark_id,
                        principalTable: "marks",
                        principalColumn: "id");
                    table.ForeignKey(
                        name: "FK_mark_subject_subjects_subject_id",
                        column: x => x.subject_id,
                        principalTable: "subjects",
                        principalColumn: "id");
                });

            migrationBuilder.CreateTable(
                name: "mark_types",
                columns: table => new
                {
                    id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    name = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    weight = table.Column<decimal>(type: "numeric", nullable: false),
                    total_column = table.Column<int>(type: "integer", nullable: false),
                    subject_id = table.Column<long>(type: "bigint", nullable: false),
                    class_id = table.Column<long>(type: "bigint", nullable: false),
                    is_deleted = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_mark_types", x => x.id);
                    table.ForeignKey(
                        name: "FK_mark_types_subjects_subject_id",
                        column: x => x.subject_id,
                        principalTable: "subjects",
                        principalColumn: "id");
                });

            migrationBuilder.CreateTable(
                name: "subject_classes",
                columns: table => new
                {
                    subjectid = table.Column<long>(name: "subject-id", type: "bigint", maxLength: 255, nullable: false),
                    classid = table.Column<long>(name: "class-id", type: "bigint", nullable: false),
                    weekly_slots = table.Column<int>(type: "integer", nullable: false),
                    max_slots_per_session = table.Column<int>(type: "integer", nullable: false),
                    prefer_consecutive = table.Column<bool>(type: "boolean", nullable: false),
                    is_main_subject = table.Column<bool>(type: "boolean", nullable: false),
                    special_slot = table.Column<int>(type: "integer", nullable: true),
                    special_room = table.Column<long>(type: "bigint", nullable: true),
                    is_deleted = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_subject_classes", x => new { x.subjectid, x.classid });
                    table.ForeignKey(
                        name: "FK_subject_classes_subjects_subject-id",
                        column: x => x.subjectid,
                        principalTable: "subjects",
                        principalColumn: "id");
                });

            migrationBuilder.CreateTable(
                name: "teacher_subject_classes",
                columns: table => new
                {
                    teacher_id = table.Column<string>(type: "text", nullable: false),
                    subject_id = table.Column<long>(type: "bigint", maxLength: 255, nullable: false),
                    class_id = table.Column<long>(type: "bigint", nullable: false),
                    is_deleted = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_teacher_subject_classes", x => new { x.teacher_id, x.subject_id, x.class_id });
                    table.ForeignKey(
                        name: "FK_teacher_subject_classes_subjects_subject_id",
                        column: x => x.subject_id,
                        principalTable: "subjects",
                        principalColumn: "id");
                });

            migrationBuilder.CreateTable(
                name: "component_points",
                columns: table => new
                {
                    id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    score = table.Column<decimal>(type: "numeric", nullable: true),
                    qualitative_score = table.Column<string>(type: "text", nullable: true),
                    column_order = table.Column<int>(type: "integer", nullable: false),
                    mark_subject_id = table.Column<long>(type: "bigint", nullable: false),
                    mark_type_id = table.Column<long>(type: "bigint", nullable: false),
                    is_pass_fail_type = table.Column<bool>(type: "boolean", nullable: false),
                    is_deleted = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_component_points", x => x.id);
                    table.ForeignKey(
                        name: "FK_component_points_mark_subject_mark_subject_id",
                        column: x => x.mark_subject_id,
                        principalTable: "mark_subject",
                        principalColumn: "id");
                    table.ForeignKey(
                        name: "FK_component_points_mark_types_mark_type_id",
                        column: x => x.mark_type_id,
                        principalTable: "mark_types",
                        principalColumn: "id");
                });

            migrationBuilder.CreateIndex(
                name: "IX_component_points_mark_subject_id",
                table: "component_points",
                column: "mark_subject_id");

            migrationBuilder.CreateIndex(
                name: "IX_component_points_mark_type_id",
                table: "component_points",
                column: "mark_type_id");

            migrationBuilder.CreateIndex(
                name: "IX_mark_subject_mark_id",
                table: "mark_subject",
                column: "mark_id");

            migrationBuilder.CreateIndex(
                name: "IX_mark_subject_subject_id",
                table: "mark_subject",
                column: "subject_id");

            migrationBuilder.CreateIndex(
                name: "IX_mark_types_subject_id",
                table: "mark_types",
                column: "subject_id");

            migrationBuilder.CreateIndex(
                name: "IX_teacher_subject_classes_subject_id",
                table: "teacher_subject_classes",
                column: "subject_id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "component_points");

            migrationBuilder.DropTable(
                name: "subject_classes");

            migrationBuilder.DropTable(
                name: "teacher_subject_classes");

            migrationBuilder.DropTable(
                name: "mark_subject");

            migrationBuilder.DropTable(
                name: "mark_types");

            migrationBuilder.DropTable(
                name: "marks");

            migrationBuilder.DropTable(
                name: "subjects");
        }
    }
}
