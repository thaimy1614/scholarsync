using subject_service.Common.Pagination;
using subject_service.Dtos.MarkType;

namespace subject_service.Services.Interfaces
{
    public interface IMarkTypeService
    {
        Task<(PagedResult<MarkTypeWithDeletedDto>, int)> GetAllMarkTypes(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(PagedResult<MarkTypeDto>, int)> GetAllExistingMarkTypes(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(MarkTypeDto, int)> GetMarkTypeById(long id);
        Task<int> AddMarkType(MarkTypeCreateUpdateDto markTypeCreateDto);
        Task<int> UpdateMarkType(long id, MarkTypeCreateUpdateDto markTypeUpdateDto);
        Task<int> DeleteMarkType(long id);
    }
}
