using subject_service.Common.Pagination;
using subject_service.Dtos.ComponentPoint;

namespace subject_service.Services.Interfaces
{
    public interface IComponentPointService
    {
        Task<(PagedResult<ComponentPointWithDeletedDto>, int)> GetAllComponentPoints(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(PagedResult<ComponentPointDto>, int)> GetAllExistingComponentPoints(int page, int pageSize, string sortColumn, bool isDescending);
        Task<(ComponentPointDto, int)> GetComponentPointById(long id);
        Task<int> AddComponentPoint(ComponentPointCreateUpdateDto componentPointCreateDto);
        Task<int> UpdateComponentPoint(long id, ComponentPointCreateUpdateDto componentPointUpdateDto);
        Task<int> DeleteComponentPoint(long id);
    }
}
