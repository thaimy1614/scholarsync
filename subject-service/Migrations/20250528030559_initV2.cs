using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace subject_service.Migrations
{
    /// <inheritdoc />
    public partial class initV2 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "is_main_subject",
                table: "subject_classes");

            migrationBuilder.DropColumn(
                name: "max_slots_per_session",
                table: "subject_classes");

            migrationBuilder.DropColumn(
                name: "prefer_consecutive",
                table: "subject_classes");

            migrationBuilder.DropColumn(
                name: "special_room",
                table: "subject_classes");

            migrationBuilder.DropColumn(
                name: "special_slot",
                table: "subject_classes");

            migrationBuilder.DropColumn(
                name: "weekly_slots",
                table: "subject_classes");

            migrationBuilder.AddColumn<bool>(
                name: "is_main_subject",
                table: "subjects",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<int>(
                name: "max_slots_per_session",
                table: "subjects",
                type: "integer",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.AddColumn<bool>(
                name: "prefer_consecutive",
                table: "subjects",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<long>(
                name: "special_room",
                table: "subjects",
                type: "bigint",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "special_slot",
                table: "subjects",
                type: "integer",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "weekly_slots",
                table: "subjects",
                type: "integer",
                nullable: false,
                defaultValue: 0);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "is_main_subject",
                table: "subjects");

            migrationBuilder.DropColumn(
                name: "max_slots_per_session",
                table: "subjects");

            migrationBuilder.DropColumn(
                name: "prefer_consecutive",
                table: "subjects");

            migrationBuilder.DropColumn(
                name: "special_room",
                table: "subjects");

            migrationBuilder.DropColumn(
                name: "special_slot",
                table: "subjects");

            migrationBuilder.DropColumn(
                name: "weekly_slots",
                table: "subjects");

            migrationBuilder.AddColumn<bool>(
                name: "is_main_subject",
                table: "subject_classes",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<int>(
                name: "max_slots_per_session",
                table: "subject_classes",
                type: "integer",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.AddColumn<bool>(
                name: "prefer_consecutive",
                table: "subject_classes",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<long>(
                name: "special_room",
                table: "subject_classes",
                type: "bigint",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "special_slot",
                table: "subject_classes",
                type: "integer",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "weekly_slots",
                table: "subject_classes",
                type: "integer",
                nullable: false,
                defaultValue: 0);
        }
    }
}
