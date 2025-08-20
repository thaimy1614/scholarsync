using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace subject_service.Migrations
{
    /// <inheritdoc />
    public partial class initV6 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "score_average",
                table: "marks");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<decimal>(
                name: "score_average",
                table: "marks",
                type: "numeric",
                nullable: true);
        }
    }
}
