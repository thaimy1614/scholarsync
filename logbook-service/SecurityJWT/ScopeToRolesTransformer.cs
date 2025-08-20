using Microsoft.AspNetCore.Authentication;
using System.Security.Claims;

namespace logbook_service.SecurityJWT
{
    public class ScopeToRolesTransformer : IClaimsTransformation
    {
        public Task<ClaimsPrincipal> TransformAsync(ClaimsPrincipal principal)
        {
            var identity = principal.Identity as ClaimsIdentity;
            if (identity == null) return Task.FromResult(principal);

            var scopeClaim = identity.FindFirst("scope");
            if (scopeClaim != null)
            {
                var roles = scopeClaim.Value.Split(' ', StringSplitOptions.RemoveEmptyEntries);
                foreach (var role in roles)
                {
                    if (!identity.HasClaim(ClaimTypes.Role, role))
                    {
                        identity.AddClaim(new Claim(ClaimTypes.Role, role));
                    }
                }
            }

            return Task.FromResult(principal);
        }
    }
}
