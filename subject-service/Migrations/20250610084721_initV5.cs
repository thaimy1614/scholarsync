using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace subject_service.Migrations
{
    /// <inheritdoc />
    public partial class initV5 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<bool>(
                name: "is_passed",
                table: "marks",
                type: "boolean",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "is_passed",
                table: "marks");
        }
    }
}
